package com.superstefo.automatic.investor.services.procedures;

import com.superstefo.automatic.investor.config.InvestProps;
import com.superstefo.automatic.investor.services.ProcedureRunner;
import com.superstefo.automatic.investor.services.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import static com.superstefo.automatic.investor.config.InvestProps.MINIMUM_INVESTMENT;
import static com.superstefo.automatic.investor.services.StateTypes.LOW_BALANCE;
@Setter
@Component
@RequiredArgsConstructor
@Slf4j
public class LowInvestorBalanceProcedure implements Startable {

    private final InvestProps investProps;
    private final WalletService wallet;
    private ProcedureRunner procedureRunner;

    @Override
    public void start() {
        wallet.updateInvestorsFreeMoneyFromServerRarely();
        if (wallet.getInvestorsFreeMoney().compareTo(MINIMUM_INVESTMENT) >= 0) {
            procedureRunner.nextRunFindLoansProcedure();
        } else {
            procedureRunner.postpone(investProps.getLowInvestorBalanceWaitingDuration(), LOW_BALANCE);
        }
    }

    @Autowired
    public void setProcedureRunner(@Lazy ProcedureRunner procedureRunner) {
        this.procedureRunner = procedureRunner;
    }
}