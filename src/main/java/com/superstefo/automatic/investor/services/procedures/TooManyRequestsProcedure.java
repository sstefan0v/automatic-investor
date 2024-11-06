package com.superstefo.automatic.investor.services.procedures;

import com.superstefo.automatic.investor.services.CycleDelayService;
import com.superstefo.automatic.investor.services.NextProcedureSelector;
import com.superstefo.automatic.investor.services.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.superstefo.automatic.investor.config.Constants.MINIMUM_INVESTMENT;
import static com.superstefo.automatic.investor.services.StateTypes.TOO_MANY_REQUESTS;

@Component
@Slf4j
@RequiredArgsConstructor
public class TooManyRequestsProcedure implements Startable {
    private final WalletService wallet;
    private final NextProcedureSelector nextProcedureSelector;
    private final CycleDelayService cycleDelayService;

    @Override
    public void start() {
        if (wallet.getInvestorsFreeMoney().compareTo(MINIMUM_INVESTMENT) >= 0) {
            nextProcedureSelector.findLoansProcedure();
        } else {
            nextProcedureSelector.lowInvestorBalanceProcedure();
        }
        cycleDelayService.postponeForTooManyRequests(TOO_MANY_REQUESTS.info);
    }
}