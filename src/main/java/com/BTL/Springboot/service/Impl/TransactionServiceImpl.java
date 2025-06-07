package com.BTL.Springboot.service.Impl;

import com.BTL.Springboot.dto.response.transaction.SaleTransactionDto;
import com.BTL.Springboot.dto.response.transaction.TransactionDto;
import com.BTL.Springboot.entity.Transaction;
import com.BTL.Springboot.repository.CustomerRepository;
import com.BTL.Springboot.repository.TransactionRepository;
import com.BTL.Springboot.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
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
        return transactionRepository.countByTransactionDateAndStatus(LocalDate.now(), "completed");
    }

    @Override
    public long getTotalSoldYesterday() {
        return transactionRepository.countByTransactionDateAndStatus(LocalDate.now().minusDays(1), "completed");
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
        if (oldValue == 0) return 0.0;
        return ((newValue - oldValue) / (double) oldValue) * 100.0;
    }

    private Double calculateChange(double oldValue, double newValue) {
        if (oldValue == 0.0) return 0.0;
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

}
