package com.superstefo.automatic.investor.services.procedures;

import com.superstefo.automatic.investor.config.InvestProps;
import com.superstefo.automatic.investor.services.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TooManyRequestsProcedureTest {

    @Mock
    private NextProcedureSelector nextProcedureSelectorMock;

    @Mock
    private CycleDelayService cycleDelayServiceMock;

    @Mock
    @SuppressWarnings("unused")
    private InvestProps investPropsMock;

    @Mock
    private WalletService walletServiceMock;

    @InjectMocks
    private TooManyRequestsProcedure tooManyRequestsProcedure;

    @Test
    void willStartFindLoansProcedureIfEnoughInvestorsMoneyIsFree() {
        when(walletServiceMock.getInvestorsFreeMoney()).thenReturn(BigDecimal.valueOf(200d));

        tooManyRequestsProcedure.start();

        verify(nextProcedureSelectorMock, times(1)).findLoansProcedure();

        verify(cycleDelayServiceMock, times(1)).postponeForTooManyRequests(anyString());
    }

    @Test
    void willStartLowInvestorBalanceProcedureIfNotEnoughInvestorsMoney() {
        when(walletServiceMock.getInvestorsFreeMoney()).thenReturn(BigDecimal.ONE);

        tooManyRequestsProcedure.start();

        verify(nextProcedureSelectorMock, times(1)).lowInvestorBalanceProcedure();

        verify(cycleDelayServiceMock, times(1)).postponeForTooManyRequests(anyString());
    }
}