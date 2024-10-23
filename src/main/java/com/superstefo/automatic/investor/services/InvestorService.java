package com.superstefo.automatic.investor.services;

import com.superstefo.automatic.investor.config.InvestProps;
import com.superstefo.automatic.investor.services.rest.model.get.loans.AllLoans;
import com.superstefo.automatic.investor.services.rest.model.get.loans.Loan;
import com.superstefo.automatic.investor.services.rest.InvestingRestAPIService;
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

    private InvestingRestAPIService investingRestAPIService;

    private Map<Integer, Loan> triedLoans = new HashMap<>();


    @Autowired
    public void setInvestingRestAPIService(InvestingRestAPIService investingRestAPIService) {
        this.investingRestAPIService = investingRestAPIService;
    }

    @Autowired
    public InvestorService(InvestProps investProps, @Qualifier("investTaskExecutor") Executor executor,
                           JobScheduler jobScheduler, WalletService wallet) {
        this.investProps = investProps;
        this.executor = executor;
        this.jobScheduler = jobScheduler;
        this.wallet = wallet;

        jobScheduler.setRunProcedure(findLoansProcedure());
    }

    private Consumer<Void> findLoansProcedure() {
        return (_) -> investInLoans(investingRestAPIService.getAvailableLoans());
    }

    private Consumer<Void> lowInvestorBalanceProcedure() {
        return (_) -> {
            BigDecimal availableMoney = getInvestorsFreeMoneyFromServer();
            wallet.setInvestorsFreeMoney(availableMoney);
            if (availableMoney.compareTo(MINIMUM_INVESTMENT) >= 0) {

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

        log.debug("Will start investing in loans:");
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
                    log.info("Waiting as loan is short term.");
                    Thread.sleep(300);
                }

            } catch (InterruptedException e) {

                log.error(" errr ");

            }
        };
    }

    private void invest(BigDecimal amountToInvest, Loan loan) {
        CompletableFuture
                .supplyAsync(() -> investingRestAPIService.doInvest(amountToInvest, loan), executor)
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
                case LOAN_SOLD -> {
                    triedLoans.put(loan.getLoanId(), loan);
                    log.debug("Loan will be skipped next time, loanId={}", loan.getLoanId());
                }
                case LOW_BALANCE -> CompletableFuture
                        .supplyAsync(this::getInvestorsFreeMoneyFromServer, executor)
                        .thenAccept(wallet::setInvestorsFreeMoney);
                case TOO_MANY_REQUESTS -> jobScheduler.setRunProcedure(tooManyRequestsProcedure());
                default -> log.error(" unhandled state: {}", state);
            }
        };
    }

    private BigDecimal getInvestorsFreeMoneyFromServer() {
        BigDecimal money = investingRestAPIService.getMainInfo().getAvailableMoney();
        log.info("Available investor's free money: {}", money);
        return money;
    }
}
