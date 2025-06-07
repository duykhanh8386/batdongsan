package com.BTL.Springboot.service.Impl;

import com.BTL.Springboot.dto.response.transaction.SaleTransactionDto;
import com.BTL.Springboot.dto.response.transaction.TransactionDto;
import com.BTL.Springboot.entity.Transaction;
import com.BTL.Springboot.repository.TransactionRepository;
import com.BTL.Springboot.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.PageRequest;

import java.awt.print.Pageable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

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
        return transactionRepository.findSalesBetween(start, end);
    }

    @Override
    public List<SaleTransactionDto> getSalesThisMonth() {
        LocalDate now = LocalDate.now();
        return transactionRepository.findSalesBetween(
                now.withDayOfMonth(1).atStartOfDay(),
                LocalDateTime.now());
    }

    @Override
    public List<SaleTransactionDto> getSalesThisYear() {
        LocalDate now = LocalDate.now();
        return transactionRepository.findSalesBetween(
                now.withDayOfYear(1).atStartOfDay(),
                LocalDateTime.now());
    }
}
