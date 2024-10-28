package com.superstefo.automatic.investor.services;

import com.superstefo.automatic.investor.config.InvestProps;
import com.superstefo.automatic.investor.services.rest.RestAPIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.superstefo.automatic.investor.config.InvestProps.MINIMUM_INVESTMENT;

@Service
@Slf4j
public class WalletService {

    private volatile BigDecimal investorsFreeMoney;
    private final InvestProps investProps;
    private final RestAPIService restAPIService;
    private final Executor executor;
    private final Lock lock = new ReentrantLock();

    public WalletService(InvestProps investProps, RestAPIService restAPIService, @Qualifier("investTaskExecutor") Executor executor) {
        this.investProps = investProps;
        this.restAPIService = restAPIService;
        this.executor = executor;
        setInvestorsFreeMoney(BigDecimal.TEN);
    }

    public BigDecimal approveLoanMoney(BigDecimal availableInLoanForInvesting) {
        lock.lock();
        try {
            BigDecimal approvedMoney = availableInLoanForInvesting.min( investProps.getHowMuchMoneyToInvest());

            approvedMoney = approvedMoney.min(investorsFreeMoney);

            if (availableInLoanForInvesting.subtract(approvedMoney).compareTo(MINIMUM_INVESTMENT)  < 0 && availableInLoanForInvesting.compareTo(approvedMoney) != 0 ){
                approvedMoney = availableInLoanForInvesting.subtract(MINIMUM_INVESTMENT);
            }
            if (approvedMoney.compareTo(MINIMUM_INVESTMENT) <  0) {
                return BigDecimal.ZERO;
            }

            return approvedMoney;
        } finally {
            lock.unlock();
        }
    }

    public void pull(BigDecimal freeMoney) {
        lock.lock();
        try {
            investorsFreeMoney = investorsFreeMoney.subtract(freeMoney);
        } finally {
            lock.unlock();
        }
    }

    public void setInvestorsFreeMoney(BigDecimal freeMoney) {
        lock.lock();
        try {
            log.info("Updating investor's available money = {} ", freeMoney);
            this.investorsFreeMoney = freeMoney;
        } finally {
            lock.unlock();
        }
    }

    public BigDecimal getInvestorsFreeMoney() {
        lock.lock();
        try {
            return this.investorsFreeMoney;
        } finally {
            lock.unlock();
        }
    }

    public void updateInvestorsFreeMoneyFromServer() {
        CompletableFuture
                .supplyAsync(restAPIService::getMainInfo, executor)
                .thenAccept((mainInfo) -> {
                    BigDecimal investorsFreeMoney  = mainInfo.getAvailableMoney();
                    setInvestorsFreeMoney(investorsFreeMoney);
                });
    }
}
