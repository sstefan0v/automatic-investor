package com.superstefo.automatic.investor.services.procedures;

import com.superstefo.automatic.investor.config.InvestProps;
import com.superstefo.automatic.investor.services.ProcedureRunner;
import com.superstefo.automatic.investor.services.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StartingProcedureTest {

    @Mock
    private WalletService walletServiceMock;

    @Mock
    private ProcedureRunner procedureRunnerMock;

    @Mock
    private InvestProps investPropsMock;

    @InjectMocks
    private StartingProcedure startingProcedure;

    @BeforeEach
    void setUp() {
        startingProcedure.setProcedureRunner(procedureRunnerMock);
        when(walletServiceMock.updateFreeInvestorsMoneyFromServer()).thenReturn(CompletableFuture.completedFuture(BigDecimal.TEN));
    }


    @Test
    void willGetFieldsInOrderToBePrinted() {
        startingProcedure.start();

        verify(investPropsMock, times(1)).getEmail();
        verify(investPropsMock, times(1)).getBaseUrl();
        verify(investPropsMock, times(1)).getPublicId();
    }

    @Test
    void willNotGetFieldsInOrderToBePrinted() {
        startingProcedure.start();

        verify(investPropsMock, times(0)).getPassword();
    }

    @Test
    void willSetNextProcedureToRun() {
        startingProcedure.start();

        verify(procedureRunnerMock).nextRunFindLoansProcedure();
    }

}



