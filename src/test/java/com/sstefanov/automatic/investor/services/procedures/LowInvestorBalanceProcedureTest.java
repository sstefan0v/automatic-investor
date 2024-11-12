package com.sstefanov.automatic.investor.services.procedures;

import com.sstefanov.automatic.investor.services.CycleDelayService;
import com.sstefanov.automatic.investor.services.NextProcedureSelector;
import com.sstefanov.automatic.investor.services.WalletService;
import com.sstefanov.automatic.investor.config.InvestProps;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LowInvestorBalanceProcedureTest {

    @Mock
    @SuppressWarnings("unused")
    private InvestProps investPropsMock;

    @Mock
    private WalletService walletServiceMock;

    @Mock
    private NextProcedureSelector nextProcedureSelectorMock;

    @Mock
    private CycleDelayService cycleDelayServiceMock;

    @InjectMocks
    private LowInvestorBalanceProcedure lowInvestorBalanceProcedure;

    @Test
    void willStartFindLoansProcedureIfEnoughInvestorsMoneyIsFree() {
        when(walletServiceMock.getInvestorsFreeMoney()).thenReturn(BigDecimal.valueOf(200d));

        lowInvestorBalanceProcedure.start();

        verify(nextProcedureSelectorMock, times(1)).findLoansProcedure();
    }

    @Test
    void willPostponeNextProcedureDueToInsufficientInvestorsMoney() {
        when(walletServiceMock.getInvestorsFreeMoney()).thenReturn(BigDecimal.ONE);

        lowInvestorBalanceProcedure.start();

        verify(cycleDelayServiceMock, times(1))
                .postponeForLowInvestorBalance(any());
    }
}