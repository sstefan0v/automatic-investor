package com.superstefo.automatic.investor.services;

import com.superstefo.automatic.investor.services.rest.RestAPIService;
import com.superstefo.automatic.investor.services.rest.model.main.info.MainInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Slf4j
@RequiredArgsConstructor
public class FreeInvestorMoneyUpdater {
    public static final int WAIT_DURATION_SECONDS = 11;

    private final RestAPIService restAPIService;

    private final Lock lock = new ReentrantLock();

    private volatile long futureInstant = 0;

    public CompletableFuture<BigDecimal> getFromServer() {
        if (isToWaitMore()) {
            log.debug("Skipping update investor's free money from server");
            return null;
        }
        postponeNextUpdate();
        return restAPIService.getMainInfoAsync().thenApply(MainInfo::getAvailableMoney);
    }

    private boolean isToWaitMore() {
        lock.lock();
        try {
            return futureInstant > System.currentTimeMillis();
        } finally {
            lock.unlock();
        }
    }

    private void postponeNextUpdate() {
        lock.lock();
        try {
            futureInstant = System.currentTimeMillis() + (WAIT_DURATION_SECONDS * 1000L);
            log.debug("Postponing {} seconds", WAIT_DURATION_SECONDS);
        } finally {
            lock.unlock();
        }
    }
}
