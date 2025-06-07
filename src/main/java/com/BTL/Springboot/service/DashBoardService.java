package com.BTL.Springboot.service;

import java.util.Map;

public interface DashBoardService {
    public long getTotalSoldToday();

    public long getTotalSoldYesterday();

    public double getRevenueThisMonth();

    public double getRevenueLastMonth();

    public long getCustomerThisYear();

    public long getCustomerLastYear();

    public long getActiveEmployees();

    public Map<String, Object> getDataComparison();
}
