package com.BTL.Springboot.service;

import com.BTL.Springboot.dto.response.transaction.SaleTransactionDto;
import com.BTL.Springboot.dto.response.transaction.TransactionDto;

import java.util.List;

public interface TransactionService {
    List<TransactionDto> getRecentActivities();

    List<SaleTransactionDto> getSalesToday();

    List<SaleTransactionDto> getSalesThisMonth();

    List<SaleTransactionDto> getSalesThisYear();
}
