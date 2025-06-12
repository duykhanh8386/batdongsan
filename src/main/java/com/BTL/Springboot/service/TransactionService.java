package com.BTL.Springboot.service;

import com.BTL.Springboot.dto.response.transaction.SaleTransactionDto;
import com.BTL.Springboot.dto.response.transaction.TransactionDto;
import com.BTL.Springboot.entity.Transaction;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface TransactionService {
    public Map<String, Object> getDataComparison();

    public long getTotalSoldToday();

    public long getTotalSoldYesterday();

    public double getRevenueThisMonth();

    public double getRevenueLastMonth();

    public long getCustomerThisYear();

    public long getCustomerLastYear();

    List<TransactionDto> getRecentActivities();

    List<SaleTransactionDto> getSalesToday();

    List<SaleTransactionDto> getSalesThisMonth();

    List<SaleTransactionDto> getSalesThisYear();

    public Map<String, Object> getReportData(String range);

    // Lấy danh sách ngày và tổng giá cho "Bán" hoặc "Cho thuê" (completed)
    List<LocalDate> getDatesByType(String type);

    List<Double> getTotalPricesByType(String type);

    // Lấy danh sách trạng thái và số lượng giao dịch
    List<String> getStatusesByType(String type);

    List<Long> getCountsByStatusAndType(String type);

    List<Map<String, Object>> getRadarIndicators();

    List<Map<String, Object>> getRadarSeries(String filter);
}
