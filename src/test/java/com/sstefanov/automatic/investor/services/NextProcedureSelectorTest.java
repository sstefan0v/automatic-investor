package com.sstefanov.automatic.investor.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.sstefanov.automatic.investor.services.ProcedureTypes.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(MockitoExtension.class)
class NextProcedureSelectorTest {

    @InjectMocks
    private NextProcedureSelector nextProcedureSelector;

    @Test
    void willInitializeWithTheRightProcedure() {
        assertThat(nextProcedureSelector.getNextProcedureToRun(), equalTo(STARTING));
    }

    @Test
    void willSetLowInvestorBalanceProcedure() {
        nextProcedureSelector.lowInvestorBalanceProcedure();
        assertThat(nextProcedureSelector.getNextProcedureToRun(), equalTo(LOW_BALANCE));
    }

    @Test
    void willSetFindLoansProcedure() {
        nextProcedureSelector.findLoansProcedure();
        assertThat(nextProcedureSelector.getNextProcedureToRun(), equalTo(FIND_LOANS));
    }

    @Test
    void willSetTooManyRequestsProcedure() {
        nextProcedureSelector.tooManyRequestsProcedure();
        assertThat(nextProcedureSelector.getNextProcedureToRun(), equalTo(TOO_MANY_REQUESTS));
    }
}