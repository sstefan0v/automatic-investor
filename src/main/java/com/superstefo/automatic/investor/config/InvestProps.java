package com.superstefo.automatic.investor.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.lang.reflect.Field;
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

    public  void printAllFields() {

        Class<?> clazz = this.getClass();


        Field[] fields = clazz.getDeclaredFields();


        for (Field field : fields) {
            field.setAccessible(true);
            try {
                log.info("{} = {}", field.getName(), field.get(this));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } finally {
                field.setAccessible(false);
            }
        }
    }
    @PostConstruct
    private void onceAtStartLog() {
        log.info("\n =================== Search for loans by criteria: =================== " +
                "\n      Sort               = -term" +
//                    "\n      Page Size          = " + props.getPageSize() +
//                    "\n      Max Remaining Term = " + props.getMaxRemainingTerm() +
//                    "\n      Min Remaining Term = " + props.getMinRemainingTerm() +
//                    "\n      Min Interest Rate  = " + props.getMinInterestRate() +
//                    "\n      Page Offset        = " + props.getPageOffset() +
//                    "\n      Hide Invested      = true" +
//                    "\n      Group Guarantee    = true" +
//                    "\n      Max Investment     = " + props.getHowMuchMoneyToInvest() +
//                    "\n      Investor ID        = " + props.getInvestorPublicId() +
                "\n ====================================================================== ");

        printAllFields();
    }
}
