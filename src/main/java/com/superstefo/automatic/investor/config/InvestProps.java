package com.superstefo.automatic.investor.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.time.LocalTime;

@ConfigurationProperties
@Getter
@RequiredArgsConstructor
public class InvestProps {
    private final String baseUrl;
    private final String email;
    private final String password;
    private final int pageSize;
    private final int maxRemainingTerm;
    private final int minRemainingTerm;
    private final double minInterestRate;
    private final int pageOffset;
    private final int pollFrequency;
    private final String publicId;
    private final int hideInvested;
    private final int groupGuarantee;
    private final BigDecimal howMuchMoneyToInvest;
    private final int lowInvestorBalanceWaitingDuration;
    private final int tooManyRequestsWaitingDuration;
    private final int waitBetweenInvestingInShortTermLoans;
    private final LocalTime workCyclesStartHour;
    private final LocalTime workCyclesFinishHour;

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
}
