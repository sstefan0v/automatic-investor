package com.superstefo.automatic.investor.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.lang.reflect.Field;
import java.util.List;

@ConfigurationProperties(prefix = "app")
@Data
@Slf4j
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

    public void printAllFields() {
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                if (isToLogField(field))
                    log.info("{} = {}", field.getName(), field.get(this));
            } catch (IllegalAccessException e) {
                log.error(e.getMessage());
            } finally {
                field.setAccessible(false);
            }
        }
    }

    private boolean isToLogField(Field field) {
        return !excludedFields.contains(field.getName());

    }

    @PostConstruct
    private void logOnceAtStart() {
        assertPrintExcludedFieldsArePresent();
        log.info("================================ Settings: ===================================");
        printAllFields();
        log.info("==============================================================================");
    }

    private List<String> excludedFields = List.of("excludedFields", "password", "log");

    private void assertPrintExcludedFieldsArePresent() {

        excludedFields.forEach(field -> {
            try {
                this.getClass().getDeclaredField(field);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("The class does not have searched field:", e);
            }
        });
    }

}
