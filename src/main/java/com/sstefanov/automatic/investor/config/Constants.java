package com.sstefanov.automatic.investor.config;

import java.math.BigDecimal;

public class Constants {
    public static final String LOGIN_URL = "v1/investor/login";
    public static final String LOANS_URL = "v1/loans/";
    public static final String OVERVIEW_URL = "v1/investor/overview";
    public static final int INVESTOR_BALANCE_UPDATE_INTERVAL_SECONDS = 16 * 1000;
    public static final String SKIP_RUN_BEFORE_WORK_HOURS = "Skip run, before work hours... ";
    public static final String SHUT_DOWN_AFTER_WORK_HOURS = "App will shut down, since it is after working hours... ";
    public static final BigDecimal MINIMUM_INVESTMENT = BigDecimal.valueOf(10);
}
