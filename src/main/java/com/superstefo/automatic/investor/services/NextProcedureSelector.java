package com.superstefo.automatic.investor.services;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.superstefo.automatic.investor.services.ProcedureTypes.*;

@Service
@Getter
@Slf4j
public class NextProcedureSelector {
    private volatile ProcedureTypes nextProcedureToRun = STARTING;

    private void setProcedureToRunNext(ProcedureTypes procedureType) {
        log.info("Next Procedure to run: {}", procedureType);
        this.nextProcedureToRun = procedureType;
    }

    public void lowInvestorBalanceProcedure() {
        setProcedureToRunNext(LOW_BALANCE);
    }

    public void findLoansProcedure() {
        setProcedureToRunNext(FIND_LOANS);
    }

    public void tooManyRequestsProcedure() {
        setProcedureToRunNext(TOO_MANY_REQUESTS);
    }
}
