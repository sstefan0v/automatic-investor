package com.superstefo.automatic.investor.services.procedures;

import com.superstefo.automatic.investor.config.InvestProps;
import com.superstefo.automatic.investor.services.ProcedureRunner;
import com.superstefo.automatic.investor.services.StateTypes;
import com.superstefo.automatic.investor.services.WalletService;
import com.superstefo.automatic.investor.services.rest.RestAPIService;
import com.superstefo.automatic.investor.services.rest.model.get.loans.AllLoans;
import com.superstefo.automatic.investor.services.rest.model.get.loans.Loan;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.superstefo.automatic.investor.config.InvestProps.MINIMUM_INVESTMENT;

@Component
@Slf4j
@Setter
@RequiredArgsConstructor
public class FindLoansProcedure implements Startable {

    private ProcedureRunner procedureRunner;
    private final InvestProps investProps;
    private final WalletService wallet;
    private final RestAPIService restAPIService;

    private final Map<Integer, Loan> triedLoans = new HashMap<>();

    @Override
    public void start() {
        if (wallet.getInvestorsFreeMoney().compareTo(MINIMUM_INVESTMENT) < 0) {
            procedureRunner.nextRunLowInvestorBalanceProcedure();
        }
        wallet.updateInvestorsFreeMoneyFromServerRarely();

        investInLoans(restAPIService.getAvailableLoans());
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
        restAPIService
                .invest(amountToInvest, loan)
                .thenAccept(actDependingOnInvestCallResult(amountToInvest, loan));
    }

    private Consumer<StateTypes> actDependingOnInvestCallResult(BigDecimal amountToInvest, Loan loan) {
        return (state) -> {
            switch (state) {
                case OK -> {
                    triedLoans.put(loan.getLoanId(), loan);
                    wallet.pull(amountToInvest);
                    wallet.updateInvestorsFreeMoneyFromServer();
                    log.debug("Invested in loan. Will be skipped next time, loanId={}", loan.getLoanId());
                }
                case LOAN_SOLD, LOAN_LESS_THAN_MIN -> {
                    triedLoans.put(loan.getLoanId(), loan);
                    log.debug("Loan will be skipped next time, loanId={}", loan.getLoanId());
                }
                case LOW_BALANCE -> wallet.updateInvestorsFreeMoneyFromServer();
                case TOO_MANY_REQUESTS ->  procedureRunner.nextRunTooManyRequestsProcedure();
                default -> log.warn("Unhandled stateType: {}", state);
            }
        };
    }

    @Autowired
    public void setProcedureRunner(@Lazy ProcedureRunner procedureRunner) {
        this.procedureRunner = procedureRunner;
    }
}