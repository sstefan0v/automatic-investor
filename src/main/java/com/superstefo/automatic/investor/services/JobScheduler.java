package com.superstefo.automatic.investor.services;

import com.superstefo.automatic.investor.services.procedures.*;
import com.superstefo.automatic.investor.services.rest.RestAPIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import static com.superstefo.automatic.investor.config.Constants.INVESTOR_BALANCE_UPDATE_INTERVAL_SECONDS;

@Service
@Slf4j
@EnableScheduling
@RequiredArgsConstructor
public class JobScheduler {
    private final RestAPIService restAPIService;
    private final WalletService walletService;
    private final TooManyRequestsProcedure tooManyRequestsProcedure;
    private final LowInvestorBalanceProcedure lowInvestorBalanceProcedure;
    private final FindLoansProcedure findLoansProcedure;
    private final StartingProcedure startingProcedure;
    private final NextProcedureSelector nextProcedureSelector;
    private final CycleDelayService cycleDelayService;

    @Scheduled(initialDelay = 1)
    public void init() {
        updateFreeInvestorsMoneyFromServer();
        startingProcedure.start();
    }

    @Scheduled(initialDelay = 100, fixedRateString = "${pollFrequency}")
    public void run() {
        if (cycleDelayService.isToWaitMore()) {
            return;
        }
        switch (nextProcedureSelector.getNextProcedureToRun()) {
            case STARTING -> startingProcedure.start();
            case FIND_LOANS -> findLoansProcedure.start();
            case LOW_BALANCE -> lowInvestorBalanceProcedure.start();
            case TOO_MANY_REQUESTS -> tooManyRequestsProcedure.start();
        }
    }

    @Scheduled(initialDelay = 10000, fixedRate = INVESTOR_BALANCE_UPDATE_INTERVAL_SECONDS)
    private void updateFreeInvestorsMoneyFromServer() {
        restAPIService.getMainInfoAsync()
                .thenAccept(mainInfo -> walletService.setInvestorsFreeMoney(mainInfo.getAvailableMoney()));
    }

    @Scheduled(initialDelay = 3000)
    public void logFreeInvestorsMoney() {
        log.info("Free investor's funds: {}", walletService.getInvestorsFreeMoney());
    }
}


