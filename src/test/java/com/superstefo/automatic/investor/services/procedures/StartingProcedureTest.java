package com.superstefo.automatic.investor.services.procedures;

import com.superstefo.automatic.investor.config.InvestProps;
import com.superstefo.automatic.investor.services.NextProcedureSelector;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StartingProcedureTest {

    @Mock
    private InvestProps investPropsMock;

    @Mock
    private  NextProcedureSelector nextProcedureSelectorMock;

    @InjectMocks
    private StartingProcedure startingProcedure;



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

        verify(nextProcedureSelectorMock).findLoansProcedure();
    }
}



