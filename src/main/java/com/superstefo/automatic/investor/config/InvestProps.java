package com.superstefo.automatic.investor.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "app")
@Getter
@RequiredArgsConstructor
public class InvestProps {
    private final String baseUrl;
    private final String email;
    private final String password;
    private final String pageSize;
    private final String maxRemainingTerm;
    private final String minRemainingTerm;
    private final String minInterestRate;
    private final String pageOffset;
    private final int pollFrequency;
    private final String publicId;
    private final String hideInvested;
    private final String groupGuarantee;
    private final BigDecimal howMuchMoneyToInvest;
    private final int lowInvestorBalanceWaitingDuration;
    private final int tooManyRequestsWaitingDuration;
    private final int waitBetweenInvestingInShortTermLoans;
    private final String workCyclesStartHour;
    private final String workCyclesFinishHour;

    public String getLoansUrl() {
        return "v1/" + publicId +
                "/loans?sort=-term" +
                "&pageSize=" + pageSize +
                "&maxRemainingTerm=" + maxRemainingTerm +
                "&minRemainingTerm=" + minRemainingTerm +
                "&minInterestRate=" + minInterestRate +
                "&hideInvested=" + hideInvested +
                "&groupGuarantee=" + groupGuarantee +
                "&offset=" + pageOffset;
    }

    public static final String LOGIN_URL = "v1/investor/login";
    public static final String LOANS_URL = "v1/loans/";
    public static final String OVERVIEW_URL = "v1/investor/overview";
    public static final BigDecimal MINIMUM_INVESTMENT = BigDecimal.valueOf(10);
}
