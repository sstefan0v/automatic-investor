package com.superstefo.automatic.investor.services;

import com.superstefo.automatic.investor.config.InvestProps;
import com.superstefo.automatic.investor.services.rest.model.get.loans.AllLoans;
import com.superstefo.automatic.investor.services.rest.model.get.loans.Loan;
import com.superstefo.automatic.investor.services.rest.RestAPIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import static com.superstefo.automatic.investor.config.InvestProps.MINIMUM_INVESTMENT;
import static com.superstefo.automatic.investor.services.StateTypes.*;

@Service
@Slf4j
public class InvestorService {

    private final InvestProps investProps;
    private final Executor executor;
    private final JobScheduler jobScheduler;
    private final WalletService wallet;
    private final RestAPIService restAPIService;

    private final Map<Integer, Loan> triedLoans = new HashMap<>();

    @Autowired
    public InvestorService(InvestProps investProps, @Qualifier("investTaskExecutor") Executor executor,
                           JobScheduler jobScheduler, WalletService wallet, RestAPIService restAPIService) {
        this.investProps = investProps;
        this.executor = executor;
        this.jobScheduler = jobScheduler;
        this.wallet = wallet;
        this.restAPIService = restAPIService;

        jobScheduler.setRunProcedure(lowInvestorBalanceProcedure());
    }

    private Consumer<Void> findLoansProcedure() {
        return (_) -> {
            if (wallet.getInvestorsFreeMoney().compareTo(MINIMUM_INVESTMENT) < 0) {
                jobScheduler.setRunProcedure(lowInvestorBalanceProcedure());
            } else if (wallet.getInvestorsFreeMoney().compareTo(investProps.getHowMuchMoneyToInvest()) < 0) {
                wallet.updateInvestorsFreeMoneyFromServer();
            }
            investInLoans(restAPIService.getAvailableLoans());
        };
    }

    private Consumer<Void> lowInvestorBalanceProcedure() {
        return (_) -> {
            wallet.updateInvestorsFreeMoneyFromServer();
            if (wallet.getInvestorsFreeMoney().compareTo(MINIMUM_INVESTMENT) >= 0) {
                jobScheduler.setRunProcedure(findLoansProcedure());
            } else {
                jobScheduler.postpone(investProps.getLowInvestorBalanceWaitingDuration(), LOW_BALANCE);
            }
        };
    }

    private Consumer<Void> tooManyRequestsProcedure() {
        return (_) -> {
            if (wallet.getInvestorsFreeMoney().compareTo(MINIMUM_INVESTMENT) >= 0) {
                jobScheduler.setRunProcedure(findLoansProcedure());
            } else {
                jobScheduler.setRunProcedure(lowInvestorBalanceProcedure());
            }
        };
    }

    private void investInLoans(AllLoans allLoans) {
        if (allLoans == null || allLoans.getData() == null || allLoans.getData().isEmpty()) {
            return;
        }

        log.debug("Loans found for investment:");
        allLoans.getData().forEach(loan -> log.info("Loan {}, from {}, at {}%", loan.getLoanId(), loan.getCountry(),
                loan.getInterestRate()));

        Collections.reverse(allLoans.getData());

        allLoans
                .getData()
                .stream()
                .filter(Loan::isAllowedToInvest)
                .filter(loan -> !triedLoans.containsKey(loan.getLoanId()))
                .forEach(getMoneyAndInvest());
    }

    private Consumer<Loan> getMoneyAndInvest() {
        return loan -> {
            BigDecimal amountToInvest = wallet.approveLoanMoney(loan.getAvailableToInvest());
            invest(amountToInvest, loan);

            try {
                if (loan.getTermType().equalsIgnoreCase("short")) {
                    log.debug("Waiting as loan is short term.");
                    Thread.sleep(investProps.getWaitBetweenInvestingInShortTermLoans());
                }
            } catch (InterruptedException e) {
                log.error("Error while waiting for loan investment. ", e);
            }
        };
    }

    private void invest(BigDecimal amountToInvest, Loan loan) {
        CompletableFuture
                .supplyAsync(() -> restAPIService.doInvest(amountToInvest, loan), executor)
                .thenAccept(workBasedOnResult(amountToInvest, loan));
    }

    private Consumer<StateTypes> workBasedOnResult(BigDecimal amountToInvest, Loan loan) {
        return (state) -> {
            switch (state) {
                case OK -> {
                    triedLoans.put(loan.getLoanId(), loan);
                    wallet.pull(amountToInvest);
                    log.debug("Invested in loan. Will be skipped next time, loanId={}", loan.getLoanId());
                }
                case LOAN_SOLD, LOAN_LESS_THAN_MIN -> {
                    triedLoans.put(loan.getLoanId(), loan);
                    log.debug("Loan will be skipped next time, loanId={}", loan.getLoanId());
                }
                case LOW_BALANCE -> {
                    //wallet.updateInvestorsFreeMoneyFromServer();
                }
                case TOO_MANY_REQUESTS -> jobScheduler.setRunProcedure(tooManyRequestsProcedure());
                default -> log.warn("Unhandled stateType: {}", state);
            }
        };
    }
}
