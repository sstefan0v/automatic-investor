package com.superstefo.automatic.investor.services;

import com.superstefo.automatic.investor.config.InvestProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.superstefo.automatic.investor.config.Constants.MINIMUM_INVESTMENT;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletService {
    private volatile BigDecimal investorsFreeMoney = BigDecimal.ZERO;
    private final InvestProps investProps;

    private final Lock lock = new ReentrantLock();

    public BigDecimal approveLoanMoney(BigDecimal availableInLoanForInvesting) {
        lock.lock();
        try {
            BigDecimal approvedMoney = availableInLoanForInvesting.min(investProps.getHowMuchMoneyToInvest());

            approvedMoney = approvedMoney.min(investorsFreeMoney);

            if (availableInLoanForInvesting.subtract(approvedMoney).compareTo(MINIMUM_INVESTMENT) < 0 && availableInLoanForInvesting.compareTo(approvedMoney) != 0) {
                approvedMoney = availableInLoanForInvesting.subtract(MINIMUM_INVESTMENT);
            }
            if (approvedMoney.compareTo(MINIMUM_INVESTMENT) < 0) {
                return BigDecimal.ZERO;
            }

            return approvedMoney;
        } finally {
            lock.unlock();
        }
    }

    public void pull(BigDecimal freeMoney) {
        setInvestorsFreeMoney(investorsFreeMoney.subtract(freeMoney));
    }

    public void setInvestorsFreeMoney(BigDecimal freeMoney) {
        lock.lock();
        try {
            log.debug("Updating investor's available money = {} ", freeMoney);
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
}
