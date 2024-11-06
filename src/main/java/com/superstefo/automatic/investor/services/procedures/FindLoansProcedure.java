package com.superstefo.automatic.investor.services.procedures;

import com.superstefo.automatic.investor.config.InvestProps;
import com.superstefo.automatic.investor.services.*;
import com.superstefo.automatic.investor.services.rest.RestAPIService;
import com.superstefo.automatic.investor.services.rest.model.get.loans.AllLoans;
import com.superstefo.automatic.investor.services.rest.model.get.loans.Loan;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.superstefo.automatic.investor.config.Constants.MINIMUM_INVESTMENT;

@Component
@Slf4j
@Setter
@RequiredArgsConstructor
public class FindLoansProcedure implements Startable {

    private final InvestProps props;
    private final WalletService wallet;
    private final RestAPIService restAPIService;
    private final NextProcedureSelector nextProcedureSelector;

    private final Map<Integer, Loan> triedLoans = new HashMap<>();

    @Override
    public void start() {
        if (wallet.getInvestorsFreeMoney().compareTo(MINIMUM_INVESTMENT) < 0) {
            nextProcedureSelector.lowInvestorBalanceProcedure();
        }
        investInLoans(restAPIService.getAvailableLoans());
    }

    private void investInLoans(AllLoans allLoans) {
        if (allLoans == null || allLoans.getData() == null || allLoans.getData().isEmpty()) {
            return;
        }

        Collections.reverse(allLoans.getData());

        log.debug("Loans found for investment:");
        allLoans.getData().forEach(loan -> log.info("Loan {}, from {}, at {}%", loan.getLoanId(), loan.getCountry(),
                loan.getInterestRate()));

        allLoans
                .getData()
                .stream()
                .filter(Loan::isAllowedToInvest)
                .forEach(getMoneyAndInvest());
    }

    private Consumer<Loan> getMoneyAndInvest() {
        return loan -> {
            if (triedLoans.containsKey(loan.getLoanId())) {
                log.debug("Will skip investment in LoanId={}.", loan.getLoanId());
                return;
            }
            BigDecimal amountToInvest = wallet.approveLoanMoney(loan.getAvailableToInvest());
            invest(amountToInvest, loan);

            try {
                if (loan.getTermType().equalsIgnoreCase("short")) {
                    Thread.sleep(props.getWaitBetweenInvestingInShortTermLoans());
                }
            } catch (InterruptedException e) {
                log.error("Error while waiting for loan investment. ", e);
            }
        };
    }

    private void invest(BigDecimal amountToInvest, Loan loan) {
        if (amountToInvest.compareTo(MINIMUM_INVESTMENT) < 0) {
            log.info("Will not invest in loan={} as investment amount is less than MIN;", loan.getLoanId());
            return;
        }
        restAPIService
                .invest(amountToInvest, loan)
                .thenAccept(actDependingOnInvestCallResult(amountToInvest, loan));
    }

    private Consumer<StateTypes> actDependingOnInvestCallResult(BigDecimal amountToInvest, Loan loan) {
        return (state) -> {
            log.debug("Investment call result={} for loanId={}", state, loan.getLoanId());
            switch (state) {
                case OK -> {
                    triedLoans.put(loan.getLoanId(), loan);
                    wallet.pull(amountToInvest);
                }
                case LOAN_SOLD, LOAN_LESS_THAN_MIN, SERVER_ERROR -> triedLoans.put(loan.getLoanId(), loan);
                case LOW_BALANCE -> nextProcedureSelector.lowInvestorBalanceProcedure();
                case TOO_MANY_REQUESTS -> nextProcedureSelector.tooManyRequestsProcedure();
                default -> log.warn("Unhandled stateType: {}", state);
            }
        };
    }
}