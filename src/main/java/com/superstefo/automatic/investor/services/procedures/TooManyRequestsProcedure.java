package com.superstefo.automatic.investor.services.procedures;

import com.superstefo.automatic.investor.config.InvestProps;
import com.superstefo.automatic.investor.services.ProcedureRunner;
import com.superstefo.automatic.investor.services.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import static com.superstefo.automatic.investor.config.Constants.MINIMUM_INVESTMENT;
import static com.superstefo.automatic.investor.services.StateTypes.TOO_MANY_REQUESTS;

@Component
@Slf4j
@RequiredArgsConstructor
public class TooManyRequestsProcedure implements Startable {
    private final WalletService wallet;
    private ProcedureRunner procedureRunner;
    private final InvestProps investProps;

    @Override
    public void start() {
        if (wallet.getInvestorsFreeMoney().compareTo(MINIMUM_INVESTMENT) >= 0) {
            procedureRunner.nextRunFindLoansProcedure();
        } else {
            procedureRunner.nextRunLowInvestorBalanceProcedure();
        }
        procedureRunner.postpone(investProps.getTooManyRequestsWaitingDuration(), TOO_MANY_REQUESTS);
    }

    @Autowired
    public void setProcedureRunner(@Lazy ProcedureRunner procedureRunner) {
        this.procedureRunner = procedureRunner;
    }
}