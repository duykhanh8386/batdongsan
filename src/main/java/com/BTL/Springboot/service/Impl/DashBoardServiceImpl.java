package com.BTL.Springboot.service.Impl;

import com.BTL.Springboot.repository.CustomerRepository;
import com.BTL.Springboot.repository.EmployeeRepository;
import com.BTL.Springboot.repository.TransactionRepository;
import com.BTL.Springboot.service.DashBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class DashBoardServiceImpl implements DashBoardService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

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
    public long getActiveEmployees() {
        return employeeRepository.countByIsActiveTrue();
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
}
