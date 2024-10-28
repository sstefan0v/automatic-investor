package com.superstefo.automatic.investor.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

@Service
@Slf4j
@EnableScheduling
public class JobScheduler {
    private final Lock lock = new ReentrantLock();

    private StateTypes state = StateTypes.OK;
    private volatile long futureInstant = 0;
    private Consumer<Void > consumer;
    private final LocalTime start = LocalTime.of(8, 30);
    private final LocalTime end = LocalTime.of(11, 0);

    public void setRunProcedure(Consumer<Void> consumer) {
        this.consumer =  consumer;
    }

    @Scheduled(initialDelay = 100, fixedRateString = "${app.pollFrequency}")
    public void start() {
        if (isToWaitMore()) {
            log.info("Waiting due to {}", state.info);
            return;
        }

        LocalTime now = LocalTime.now();

        if (now.isBefore(start) || now.isAfter(end)) {
            log.debug("Skipping run, since it is outside of work hours ");
            return;
        }
        consumer.accept(null);
    }

    private boolean isToWaitMore() {
        lock.lock();
        try {
            return futureInstant > System.currentTimeMillis();
        } finally {
            lock.unlock();
        }
    }

    public void postpone(int seconds, StateTypes state) {
        lock.lock();
        try {
            futureInstant = System.currentTimeMillis() + (seconds * 1000L);
            this.state = state;
            log.debug("Postponing {} seconds due to {}", seconds, state);
        } finally {
            lock.unlock();
        }
    }
}


