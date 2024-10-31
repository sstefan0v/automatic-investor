package com.superstefo.automatic.investor.services;

import com.superstefo.automatic.investor.services.rest.RestAPIService;
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
public class InvestorFreeMoneyUpdater {
    public static final int WAIT_DURATION_SECONDS = 10;

    private final RestAPIService restAPIService;

    private final Lock lock = new ReentrantLock();

    private volatile long futureInstant = 0;

    public CompletableFuture<BigDecimal> getFromServer() {
       return restAPIService.getMainInfoAsync()
                .thenApply((mainInfo)->{
                    postponeNextUpdate();
                    return mainInfo.getAvailableMoney();
                });
    }

    public CompletableFuture<BigDecimal> getFromServerRarely() {
        if (isToWaitMore()) {
               log.debug("Waiting to update investor's free money from server");
            return null;
        }
       return getFromServer();
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
