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
    private final OneLineLogger oneLineLogger = OneLineLogger.create();

    private final TooManyRequestsProcedure tooManyRequestsProcedure;
    private final LowInvestorBalanceProcedure lowInvestorBalanceProcedure;
    private final FindLoansProcedure findLoansProcedure;
    private final StartingProcedure startingProcedure;
    private final InvestProps props;

    private StateTypes state = StateTypes.OK;
    private volatile long futureInstant = 0;

    private Startable procedureToRunNext;
    private LocalTime startHour;
    private LocalTime finishHour;

    @PostConstruct
    public void init() {
        startHour = props.getWorkCyclesStartHour();
        finishHour = props.getWorkCyclesFinishHour();
        setProcedureToRunNext(startingProcedure);
    }

    @Scheduled(initialDelay = 100, fixedRateString = "${pollFrequency}")
    public void run() {
        if (isToWaitMore()) {
            log.debug("Waiting due to {}", state.info);
            return;
        }

        LocalTime now = LocalTime.now();

        if (now.isBefore(startHour)) {
            oneLineLogger.print("Skip, not yet in work hours:",".");
            log.debug("Skipping run, not yet in work hours ");
            return;
        } else if (now.isAfter(finishHour)){
            log.info("App will shut down, since it is after working hours... ");
            System.exit(0);
        }
        procedureToRunNext.start();
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
            log.info("Postponing next run for {} seconds due to {}", seconds, state);
        } finally {
            lock.unlock();
        }
    }

    public void nextRunLowInvestorBalanceProcedure() {
        setProcedureToRunNext(lowInvestorBalanceProcedure);
    }

    public void nextRunFindLoansProcedure() {
        setProcedureToRunNext(findLoansProcedure);
    }

    public void nextRunTooManyRequestsProcedure() {
        setProcedureToRunNext(tooManyRequestsProcedure);
    }

    public void setProcedureToRunNext(Startable procedureToRunNext) {
        log.info("Next Procedure to run: {}", procedureToRunNext.getClass().getSimpleName());
        this.procedureToRunNext = procedureToRunNext;
    }
}


