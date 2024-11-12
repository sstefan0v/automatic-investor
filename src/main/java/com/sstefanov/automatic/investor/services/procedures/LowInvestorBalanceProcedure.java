package com.sstefanov.automatic.investor.services.procedures;

import com.sstefanov.automatic.investor.services.NextProcedureSelector;
import com.sstefanov.automatic.investor.services.CycleDelayService;
import com.sstefanov.automatic.investor.services.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.sstefanov.automatic.investor.config.Constants.MINIMUM_INVESTMENT;

@Setter
@Component
@RequiredArgsConstructor
@Slf4j
public class LowInvestorBalanceProcedure implements Startable {

    private final WalletService wallet;
    private final NextProcedureSelector nextProcedureSelector;
    private final CycleDelayService cycleDelayService;

    @Override
    public void start() {
        if (wallet.getInvestorsFreeMoney().compareTo(MINIMUM_INVESTMENT) >= 0) {
            nextProcedureSelector.findLoansProcedure();
        } else {
            cycleDelayService.postponeForLowInvestorBalance("Free investor's funds: " + wallet.getInvestorsFreeMoney());
        }
    }
}