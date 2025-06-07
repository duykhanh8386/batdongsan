package com.BTL.Springboot.service;

import com.BTL.Springboot.dto.response.transaction.SaleTransactionDto;
import com.BTL.Springboot.dto.response.transaction.TransactionDto;

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
}
