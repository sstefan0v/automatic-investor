package com.sstefanov.automatic.investor.services.rest.model.main.info;

import java.math.BigDecimal;

@lombok.Data
public class MainInfo{
    private String currencyIso;
    private BigDecimal availableMoney;
    private String invested;
    private String totalProfit;
    private String totalBalance;
    private String balanceGrowth;
    private String balanceGrowthAmount;
}
