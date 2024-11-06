package com.superstefo.automatic.investor.services;

import com.superstefo.automatic.investor.config.InvestProps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static com.superstefo.automatic.investor.config.Constants.MINIMUM_INVESTMENT;
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
        setup(900d);
        BigDecimal approvedMoney = walletService.approveLoanMoney(BigDecimal.valueOf(15.2));
        assertThat(approvedMoney, equalTo(BigDecimal.valueOf(15.2)));
    }

    @Test
    void run2() {
        setup(900d);
        BigDecimal approvedMoney = walletService.approveLoanMoney(BigDecimal.valueOf(605.2));
        assertThat(approvedMoney, equalTo(MAX_INVEST_SUM));
    }

    @Test
    void run3() {
        setup(11d);
        BigDecimal approvedMoney = walletService.approveLoanMoney(BigDecimal.valueOf(605.2));
        assertThat(approvedMoney, equalTo(BigDecimal.valueOf(11)));
    }

    @Test
    void run4() {
        setup(9.8d);
        BigDecimal approvedMoney = walletService.approveLoanMoney(BigDecimal.valueOf(15.2));
        assertThat(approvedMoney, equalTo(BigDecimal.valueOf(0)));
    }

    @Test
    void run5() {
        setup(11d);
        BigDecimal approvedMoney = walletService.approveLoanMoney(BigDecimal.valueOf(15.2));
        assertThat(approvedMoney, equalTo(BigDecimal.valueOf(0)));
    }

    @Test
    void run6() {
        setup(11d);
        BigDecimal approvedMoney = walletService.approveLoanMoney(BigDecimal.valueOf(20));
        assertThat(approvedMoney, equalTo(BigDecimal.valueOf(10)));
    }

    @Test
    void run7() {
        setup(16d);
        BigDecimal approvedMoney = walletService.approveLoanMoney(BigDecimal.valueOf(13));
        assertThat(approvedMoney, equalTo(BigDecimal.valueOf(13)));
    }

    @Test
    void run8() {
        setup(190d);
        BigDecimal approvedMoney = walletService.approveLoanMoney(BigDecimal.valueOf(199));
        assertThat(approvedMoney, equalTo(BigDecimal.valueOf(199).subtract(MINIMUM_INVESTMENT)));
    }

    @Test
    void run9() {
        setup(21d);
        BigDecimal approvedMoney = walletService.approveLoanMoney(BigDecimal.valueOf(20));
        assertThat(approvedMoney, equalTo(BigDecimal.valueOf(20)));
    }

    void setup(double investorsFreeMoney) {
        walletService.setInvestorsFreeMoney(new BigDecimal(investorsFreeMoney));
    }
}