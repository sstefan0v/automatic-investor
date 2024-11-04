package com.superstefo.automatic.investor.services.rest;

import com.superstefo.automatic.investor.config.InvestProps;
import com.superstefo.automatic.investor.services.OneLineLogger;
import com.superstefo.automatic.investor.services.rest.model.get.loans.AllLoans;
import com.superstefo.automatic.investor.services.rest.model.get.loans.Loan;
import com.superstefo.automatic.investor.services.rest.model.invest.Invest;
import com.superstefo.automatic.investor.services.rest.model.main.info.MainInfo;
import com.superstefo.automatic.investor.services.StateTypes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.superstefo.automatic.investor.config.InvestProps.LOANS_URL;
import static com.superstefo.automatic.investor.config.InvestProps.OVERVIEW_URL;
import static com.superstefo.automatic.investor.services.rest.HttpHeaderUtils.getAuthHeaders;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Service
@Slf4j
public final class RestAPIService extends RestAPIConnector {

    private final InvestProps investProps;
    private final Executor executor;
    private final MultiValueMap<String, String> investingUserAuthHeaders;
    private final OneLineLogger oneLineLogger = OneLineLogger.create();

    RestAPIService(InvestProps investProps, @Qualifier("investTaskExecutor") Executor executor) {
        super(investProps);
        this.investProps = investProps;
        this.executor = executor;
        confirmHostReachable();
        String authToken = this.getAuthTokenFromEndpoint(investProps.getEmail(), investProps.getPassword());
        this.investingUserAuthHeaders = getAuthHeaders(investProps, "Bearer " + authToken);
    }

    public CompletableFuture<StateTypes> invest(BigDecimal amountToInvest, Loan loan) {
        return CompletableFuture.supplyAsync(() -> doInvest(amountToInvest, loan), executor);
    }

    private StateTypes doInvest(BigDecimal amount, Loan loan) {
        log.info("Investing in loanId={}; availableToInvest={}, myAmount={}",
                loan.getLoanId(), loan.getAvailableToInvest(), amount);

        try {
            this.exchange(
                    LOANS_URL + loan.getLoanId(),
                    POST,
                    new HttpEntity<>(Invest.getNew(amount.toString()), this.investingUserAuthHeaders),
                    String.class);
        } catch (RuntimeException exception) {
            return getResultStateBasedOnException(exception, loan);
        }
        log.info("{} EUR invested in loan {};", amount, loan.getLoanId());
        return StateTypes.OK;
    }

    private StateTypes getResultStateBasedOnException(RuntimeException exc, Loan loan) {
        return
                switch (exc) {
                    case HttpClientErrorException.TooManyRequests _ -> StateTypes.TOO_MANY_REQUESTS;
                    case HttpClientErrorException.BadRequest e -> {
                        log.error("LoanId={} returned BadRequest Exception: {}", loan.getLoanId(), e.getMessage());
                        yield StateTypes.fromErrorMessage(e);
                    }
                    case HttpServerErrorException e -> {
                        log.error("LoanId={} returned InternalServerError Exception: {}", loan.getLoanId(), e.getMessage());
                        yield StateTypes.SERVER_ERROR;
                    }
                    default -> throw exc;
                };
    }

    public CompletableFuture<MainInfo> getMainInfoAsync() {
        return CompletableFuture.supplyAsync(this::getMainInfo, executor);
    }

    public MainInfo getMainInfo() {
        MainInfo mainInfo = this.exchange(
                OVERVIEW_URL,
                GET,
                new HttpEntity<>("", this.investingUserAuthHeaders),
                MainInfo.class);
        log.debug("Main info: {}", mainInfo);
        return mainInfo;
    }

    @SuppressWarnings("unused")
    public String getLoanInfo(String id) {
        String res = this.exchange(
                LOANS_URL + id,
                GET,
                new HttpEntity<>("", this.investingUserAuthHeaders),
                String.class);
        log.info("Loan info: {}", res);
        return res;
    }

    public AllLoans getAvailableLoans() {
        AllLoans allLoans = this.exchange(
                investProps.getLoansUrl(),
                HttpMethod.GET,
                new HttpEntity<>("", this.investingUserAuthHeaders),
                AllLoans.class);
        int foundLoansSize = allLoans.getData() != null ? allLoans.getData().size() : allLoans.getTotal();
        log.debug("Found loans: {}", foundLoansSize);
        oneLineLogger.print("Found loans: ", "" + foundLoansSize);
        return allLoans;
    }
}
