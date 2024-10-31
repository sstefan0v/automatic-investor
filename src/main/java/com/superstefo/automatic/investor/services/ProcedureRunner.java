package com.superstefo.automatic.investor.services;

import com.superstefo.automatic.investor.services.procedures.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
@EnableScheduling
@RequiredArgsConstructor
public class ProcedureRunner {
    private final Lock lock = new ReentrantLock();

    private final TooManyRequestsProcedure tooManyRequestsProcedure;
    private final LowInvestorBalanceProcedure lowInvestorBalanceProcedure;
    private final FindLoansProcedure findLoansProcedure;
    private final StartingProcedure startingProcedure;

    private StateTypes state = StateTypes.OK;
    private volatile long futureInstant = 0;

    private Startable whatToRunNext;
    private final LocalTime start = LocalTime.parse("08:30");
    private final LocalTime end = LocalTime.parse("23:15");

    @PostConstruct
    public void init() {
        setWhatToRunNext(startingProcedure);
    }

    @Scheduled(initialDelay = 100, fixedRateString = "${app.pollFrequency}")
    public void run() {
        if (isToWaitMore()) {
            log.debug("Waiting due to {}", state.info);
            return;
        }

        LocalTime now = LocalTime.now();

        if (now.isBefore(start) || now.isAfter(end)) {
            log.debug("Skipping run, since it is outside of work hours ");
            return;
        }
        whatToRunNext.start();
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

    public void nextRunLowInvestorBalanceProcedure() {
        setWhatToRunNext(lowInvestorBalanceProcedure);
    }

    public void nextRunFindLoansProcedure() {
        setWhatToRunNext(findLoansProcedure);
    }

    public void nextRunTooManyRequestsProcedure() {
        setWhatToRunNext(tooManyRequestsProcedure);
    }

    private void setWhatToRunNext(Startable whatToRunNext) {
        log.debug("Next Procedure: {}", whatToRunNext.getClass().getSimpleName());
        this.whatToRunNext = whatToRunNext;
    }
}


