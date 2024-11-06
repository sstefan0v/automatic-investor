package com.superstefo.automatic.investor.services.procedures;

import com.superstefo.automatic.investor.config.InvestProps;
import com.superstefo.automatic.investor.services.ProcedureRunner;
import com.superstefo.automatic.investor.services.StateTypes;
import com.superstefo.automatic.investor.services.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class TooManyRequestsProcedureTest {

    @Mock
    private ProcedureRunner procedureRunnerMock;

    @Mock
    @SuppressWarnings("unused")
    private InvestProps investPropsMock;

    @Mock
    private WalletService walletServiceMock;

    @InjectMocks
    private TooManyRequestsProcedure tooManyRequestsProcedure;

    @Captor
    private ArgumentCaptor<StateTypes> stateTypesArgumentCaptor;

    @Captor
    private ArgumentCaptor<Integer> integerArgumentCaptor;

    @BeforeEach
    void setUp() {
        tooManyRequestsProcedure.setProcedureRunner(procedureRunnerMock);
    }

    @Test
    void willStartFindLoansProcedureIfEnoughInvestorsMoneyIsFree() {
        when(walletServiceMock.getInvestorsFreeMoney()).thenReturn(BigDecimal.valueOf(200d));

        tooManyRequestsProcedure.start();

        verify(procedureRunnerMock, times(1)).nextRunFindLoansProcedure();

        verify(procedureRunnerMock, times(1))
                .postpone(integerArgumentCaptor.capture(),stateTypesArgumentCaptor.capture());

        assertThat(stateTypesArgumentCaptor.getValue(), equalTo(StateTypes.TOO_MANY_REQUESTS));
    }

    @Test
    void willStartLowInvestorBalanceProcedureIfNotEnoughInvestorsMoney() {
        when(walletServiceMock.getInvestorsFreeMoney()).thenReturn(BigDecimal.ONE);

        tooManyRequestsProcedure.start();

        verify(procedureRunnerMock, times(1)).nextRunLowInvestorBalanceProcedure();

        verify(procedureRunnerMock, times(1))
                .postpone(integerArgumentCaptor.capture(),stateTypesArgumentCaptor.capture());

        assertThat(stateTypesArgumentCaptor.getValue(), equalTo(StateTypes.TOO_MANY_REQUESTS));
    }
}