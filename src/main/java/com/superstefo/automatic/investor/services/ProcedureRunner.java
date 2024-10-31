package com.superstefo.automatic.investor.services;

import com.superstefo.automatic.investor.config.InvestProps;
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
    private final InvestProps props;

    private StateTypes state = StateTypes.OK;
    private volatile long futureInstant = 0;

    private Startable whatToRunNext;
    private LocalTime startHour;
    private LocalTime finishHour;

    @PostConstruct
    public void init() {
        startHour = LocalTime.parse(props.getWorkCyclesStartHour());
        finishHour = LocalTime.parse(props.getWorkCyclesFinishHour());
        setWhatToRunNext(startingProcedure);
    }

    @Scheduled(initialDelay = 100, fixedRateString = "${app.pollFrequency}")
    public void run() {
        if (isToWaitMore()) {
            log.debug("Waiting due to {}", state.info);
            return;
        }

        LocalTime now = LocalTime.now();

        if (now.isBefore(startHour) || now.isAfter(finishHour)) {
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
            log.debug("Postponing next run for {} seconds due to {}", seconds, state);
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
        log.info("Next Procedure to run: {}", whatToRunNext.getClass().getSimpleName());
        this.whatToRunNext = whatToRunNext;
    }
}


