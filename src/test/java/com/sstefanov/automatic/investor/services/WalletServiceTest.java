package com.sstefanov.automatic.investor.services;

import com.sstefanov.automatic.investor.config.InvestProps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static com.sstefanov.automatic.investor.config.Constants.MINIMUM_INVESTMENT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private InvestProps props;

    @InjectMocks
    private WalletService walletService;

    public static final BigDecimal MAX_INVEST_SUM = BigDecimal.valueOf(200);

    @BeforeEach
    void before() {
        when(props.getHowMuchMoneyToInvest()).thenReturn(MAX_INVEST_SUM);
    }

    @Test
    void run1() {
        setup("900");
        BigDecimal approvedMoney = walletService.approveLoanMoney(new BigDecimal("15.2"));
        assertThat(approvedMoney, equalTo(new BigDecimal("15.2")));
    }

    @Test
    void run2() {
        setup("900");
        BigDecimal approvedMoney = walletService.approveLoanMoney(new BigDecimal("605.2"));
        assertThat(approvedMoney, equalTo(MAX_INVEST_SUM));
    }

    @Test
    void run3() {
        setup("11");
        BigDecimal approvedMoney = walletService.approveLoanMoney(new BigDecimal("605.2"));
        assertThat(approvedMoney, equalTo(new BigDecimal("11")));
    }

    @Test
    void run4() {
        setup("9.8");
        BigDecimal approvedMoney = walletService.approveLoanMoney(new BigDecimal("15.2"));
        assertThat(approvedMoney, equalTo(new BigDecimal("0")));
    }

    @Test
    void run5() {
        setup("11");
        BigDecimal approvedMoney = walletService.approveLoanMoney(new BigDecimal("15.2"));
        assertThat(approvedMoney, equalTo(new BigDecimal("0")));
    }

    @Test
    void run6() {
        setup("11");
        BigDecimal approvedMoney = walletService.approveLoanMoney(new BigDecimal("20"));
        assertThat(approvedMoney, equalTo(new BigDecimal("10")));
    }

    @Test
    void run7() {
        setup("16");
        BigDecimal approvedMoney = walletService.approveLoanMoney(new BigDecimal("13"));
        assertThat(approvedMoney, equalTo(new BigDecimal("13")));
    }

    @Test
    void run8() {
        setup("190");
        BigDecimal approvedMoney = walletService.approveLoanMoney(new BigDecimal("199"));
        assertThat(approvedMoney, equalTo(new BigDecimal("199").subtract(MINIMUM_INVESTMENT)));
    }

    @Test
    void run9() {
        setup("21");
        BigDecimal approvedMoney = walletService.approveLoanMoney(new BigDecimal("20"));
        assertThat(approvedMoney, equalTo(new BigDecimal("20")));
    }

    @Test
    void willPullMoneyCorrectly() {
        setup("21");
        walletService.pull(walletService.approveLoanMoney(BigDecimal.TEN));
        assertThat(walletService.getInvestorsFreeMoney(), equalTo(new BigDecimal("11")));
    }

    void setup(String investorsFreeMoney) {
        walletService.setInvestorsFreeMoney(new BigDecimal(investorsFreeMoney));
    }
}