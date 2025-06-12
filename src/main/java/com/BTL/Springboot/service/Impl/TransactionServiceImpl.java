package com.BTL.Springboot.service.Impl;

import com.BTL.Springboot.dto.response.transaction.SaleTransactionDto;
import com.BTL.Springboot.dto.response.transaction.TransactionDto;
import com.BTL.Springboot.entity.Transaction;
import com.BTL.Springboot.repository.CustomerRepository;
import com.BTL.Springboot.repository.TransactionRepository;
import com.BTL.Springboot.service.TransactionService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public long getTotalSoldToday() {
        return transactionRepository.countByTransactionDateAndStatus(LocalDate.now(), "COMPLETED");
    }

    @Override
    public long getTotalSoldYesterday() {
        return transactionRepository.countByTransactionDateAndStatus(LocalDate.now().minusDays(1), "COMPLETED");
    }

    @Override
    public double getRevenueThisMonth() {
        return transactionRepository.sumRevenueThisMonth();
    }

    @Override
    public double getRevenueLastMonth() {
        return transactionRepository.sumRevenueLastMonth();
    }

    @Override
    public long getCustomerThisYear() {
        return customerRepository.countByYear(LocalDate.now().getYear());
    }

    @Override
    public long getCustomerLastYear() {
        return customerRepository.countByYear(LocalDate.now().minusYears(1).getYear());
    }

    @Override
    public Map<String, Object> getDataComparison() {
        Map<String, Object> data = new HashMap<>();

        long soldToday = getTotalSoldToday();
        long soldYesterday = getTotalSoldYesterday();
        double revenueThisMonth = getRevenueThisMonth();
        double revenueLastMonth = getRevenueLastMonth();
        long customersThisYear = getCustomerThisYear();
        long customersLastYear = getCustomerLastYear();

        data.put("soldToday", soldToday);
        data.put("soldChangePercent", calculateChange(soldYesterday, soldToday));

        data.put("monthlyRevenue", revenueThisMonth);
        data.put("revenueChangePercent", calculateChange(revenueLastMonth, revenueThisMonth));

        data.put("customersThisYear", customersThisYear);
        data.put("customerChangePercent", calculateChange(customersLastYear, customersThisYear));

        return data;
    }

    private Double calculateChange(long oldValue, long newValue) {
        if (oldValue == 0.0 && newValue>0) return 100.0;
        if (oldValue == 0.0  && newValue==0) return 0.0;
        if(oldValue > 0 && newValue ==0) return -100.0;
        return ((newValue - oldValue) / (double) oldValue) * 100.0;
    }

    private Double calculateChange(double oldValue, double newValue) {
        if (oldValue == 0.0 && newValue>0) return 100.0;
        if (oldValue == 0.0  && newValue==0) return 0.0;
        if(oldValue > 0 && newValue ==0) return -100.0;
        return ((newValue - oldValue) / oldValue) * 100.0;
    }

    public List<TransactionDto> getRecentActivities() {
        PageRequest top5 = PageRequest.of(0, 5);
        List<Transaction> recent = transactionRepository.findRecentTransactions(top5);

        return recent.stream()
                .map(t -> new TransactionDto(
                        t.getAgent().getFirstName() + " " + t.getAgent().getLastName(),
                        t.getPrice(),
                        t.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SaleTransactionDto> getSalesToday() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDateTime.now();
        Pageable top10 = PageRequest.of(0, 10);
        return transactionRepository.findSalesBetween(start, end,top10);
    }

    @Override
    public List<SaleTransactionDto> getSalesThisMonth() {
        LocalDate now = LocalDate.now();
        Pageable top10 = PageRequest.of(0, 10);
        return transactionRepository.findSalesBetween(
                now.withDayOfMonth(1).atStartOfDay(),
                LocalDateTime.now(),top10);
    }

    @Override
    public List<SaleTransactionDto> getSalesThisYear() {
        LocalDate now = LocalDate.now();
        Pageable top10 = PageRequest.of(0, 10);
        return transactionRepository.findSalesBetween(
                now.withDayOfYear(1).atStartOfDay(),
                LocalDateTime.now(),top10);
    }

    @Override
    public Map<String, Object> getReportData(String range) {
        List<String> categories = new ArrayList<>();
        List<Long> sold = new ArrayList<>();
        List<Double> revenue = new ArrayList<>();
        List<Long> customers = new ArrayList<>();
        String label = "";

        switch (range) {
            case "today":
                label = "Hôm nay";
                for (int hour = 0; hour < 24; hour++) {
                    LocalDateTime start = LocalDate.now().atTime(hour, 0);
                    LocalDateTime end = start.plusHours(1);
                    categories.add(start.toString());

                    sold.add(transactionRepository.countSoldBetween(start, end));
                    revenue.add(transactionRepository.sumRevenueBetween(start, end));
                    customers.add(customerRepository.getNewCustomersBetween(start, end));
                }
                break;

            case "month":
                label = "Tháng này";
                LocalDate now = LocalDate.now();
                int daysInMonth = now.lengthOfMonth();

                for (int day = 1; day <= daysInMonth; day++) {
                    LocalDate date = now.withDayOfMonth(day);
                    LocalDateTime start = date.atStartOfDay();
                    LocalDateTime end = start.plusDays(1);
                    LocalDate start_1 = start.toLocalDate();
                    LocalDate end_1 = end.toLocalDate();

                    categories.add(date.toString());
                    sold.add(transactionRepository.countSoldByTransactionDateBetween(start_1, end_1));
                    revenue.add(transactionRepository.sumRevenueByTransactionDateBetween(start_1, end_1));
                    customers.add(customerRepository.getNewCustomersBetween(start, end));
                }
                break;

            case "year":
                label = "Năm nay";
                for (int month = 1; month <= 12; month++) {
                    LocalDate startOfMonth = LocalDate.of(LocalDate.now().getYear(), month, 1);
                    LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1); // Ngày cuối cùng của tháng

                    LocalDateTime start = startOfMonth.atStartOfDay();
                    LocalDateTime end = endOfMonth.atTime(23, 59, 59); // Bao gồm cả ngày cuối

                    // Hoặc nếu query database cần LocalDate
                    LocalDate start_1 = startOfMonth;
                    LocalDate end_1 = startOfMonth.plusMonths(1); // Ngày đầu tháng sau (exclusive)

                    // Format category cho frontend - chỉ năm-thán
                    categories.add(endOfMonth.toString()); // → "2025-01"

                    sold.add(transactionRepository.countSoldByTransactionDateBetween(start_1, end_1));
                    revenue.add(transactionRepository.sumRevenueByTransactionDateBetween(start_1, end_1));
                    customers.add(customerRepository.getNewCustomersBetween(start, end));

                    // Debug log
                    if(month == 1){
                        System.out.println("Month: " + month);
                        System.out.println("Start: " + start_1);
                        System.out.println("End: " + end_1);
                        System.out.println("Category: " + categories.get(categories.size()-1));
                        System.out.println("Customers: " + customers.get(customers.size()-1));
                    }
                }
                break;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("categories", categories);
        result.put("sold", sold);
        result.put("revenue", revenue);
        result.put("customers", customers);
        result.put("label", label);
        return result;
    }

    //
    @Override
    public List<LocalDate> getDatesByType(String type) {
        List<Object[]> results = transactionRepository.findTotalPriceByDateAndType(type);
        List<LocalDate> dates = new ArrayList<>();
        for (Object[] result : results) {
            dates.add((LocalDate) result[0]);
        }
        return dates;
    }

    @Override
    public List<Double> getTotalPricesByType(String type) {
        List<Object[]> results = transactionRepository.findTotalPriceByDateAndType(type);
        List<Double> prices = new ArrayList<>();
        for (Object[] result : results) {
            prices.add((Double) result[1]);
        }
        return prices;
    }

    // Lấy danh sách trạng thái và số lượng giao dịch
    @Override
    public List<String> getStatusesByType(String type) {
        List<Object[]> results = transactionRepository.countTransactionsByStatusAndType(type);
        List<String> statuses = new ArrayList<>();
        for (Object[] result : results) {
            statuses.add((String) result[0]);
        }
        return statuses;
    }

    @Override
    public List<Long> getCountsByStatusAndType(String type) {
        List<Object[]> results = transactionRepository.countTransactionsByStatusAndType(type);
        List<Long> counts = new ArrayList<>();
        for (Object[] result : results) {
            counts.add((Long) result[1]);
        }
        return counts;
    }

    @Override
    public List<Map<String, Object>> getRadarIndicators() {
        List<String> statuses = transactionRepository.findAllStatuses();
        List<Map<String, Object>> indicators = new ArrayList<>();

        // Tạo map ánh xạ tên từ database sang tên hiển thị
        Map<String, String> statusDisplayNames = new HashMap<>();
        statusDisplayNames.put("COMPLETED", "Hoàn thành");
        statusDisplayNames.put("PENDING", "Chờ xử lý");
        statusDisplayNames.put("SIGNED", "Đã ký");
        statusDisplayNames.put("DEPOSITED", "Đã đặt cọc");
        statusDisplayNames.put("IN_PROGRESS", "Đang thực hiện");
        statusDisplayNames.put("TRANSFERRED", "Đã chuyển nhượng");

        for (String status : statuses) {
            Map<String, Object> indicator = new HashMap<>();
            // Sử dụng tên hiển thị, nếu không có thì dùng tên gốc
            String displayName = statusDisplayNames.getOrDefault(status, status);
            indicator.put("name", displayName);
            indicator.put("max", 50);
            indicators.add(indicator);
        }
        return indicators;
    }

    // Lấy series (dữ liệu cho radar chart)
    @Override
    public List<Map<String, Object>> getRadarSeries(String statusFilter) {
        // Xác định khoảng thời gian dựa trên statusFilter
        LocalDate startDate = null;
        LocalDate endDate = null;
        LocalDate today = LocalDate.now();

        switch (statusFilter) {
            case "today":
                startDate = today;
                endDate = today;
                break;
            case "thisMonth":
                startDate = today.withDayOfMonth(1);
                endDate = today;
                break;
            case "thisYear":
                startDate = today.withDayOfYear(1);
                endDate = today;
                break;
            default:
                // Không lọc thời gian nếu filter không hợp lệ
                break;
        }

        // Lấy dữ liệu từ repository
        List<Object[]> results = transactionRepository.countTransactionsByStatusAndType(startDate, endDate);
        Map<String, List<Long>> dataMap = new HashMap<>();
        dataMap.put("Bán", new ArrayList<>());
        dataMap.put("Cho thuê", new ArrayList<>());

        // Khởi tạo danh sách với 0 cho tất cả trạng thái
        List<String> statuses = transactionRepository.findAllStatuses();
        for (List<Long> values : dataMap.values()) {
            for (int i = 0; i < statuses.size(); i++) {
                values.add(0L);
            }
        }

        // Điền dữ liệu từ kết quả truy vấn
        for (Object[] result : results) {
            String status = (String) result[0];
            String type = (String) result[1];
            Long count = (Long) result[2];
            int index = statuses.indexOf(status);
            if (index >= 0 && dataMap.containsKey(type)) {
                dataMap.get(type).set(index, count);
            }
        }

        // Chuẩn bị dữ liệu series
        List<Map<String, Object>> series = new ArrayList<>();
        Map<String, Object> ban = new HashMap<>();
        ban.put("name", "Bán");
        ban.put("value", dataMap.get("Bán"));
        series.add(ban);

        Map<String, Object> choThue = new HashMap<>();
        choThue.put("name", "Cho thuê");
        choThue.put("value", dataMap.get("Cho thuê"));
        series.add(choThue);

        return series;
    }
}
