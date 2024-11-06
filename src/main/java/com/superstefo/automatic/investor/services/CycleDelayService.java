package com.superstefo.automatic.investor.services;

import com.superstefo.automatic.investor.config.InvestProps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class CycleDelayService {
    private final Lock lock = new ReentrantLock();
    private final OneLineLogger oneLineLogger = OneLineLogger.create();
    private volatile long futureInstant = 0L;

    private final InvestProps props;
    private final LocalTime startHour;
    private final LocalTime finishHour;

    @Autowired
    public CycleDelayService(InvestProps props) {
        this.props = props;
        startHour = props.getWorkCyclesStartHour();
        finishHour = props.getWorkCyclesFinishHour();
    }

    public boolean isToWaitMore() {
        if (isOutOfWorkHours()) return true;

        lock.lock();
        try {
            return futureInstant > System.currentTimeMillis();
        } finally {
            lock.unlock();
        }
    }

    private boolean isOutOfWorkHours() {
        LocalTime now = LocalTime.now();
        if (now.isBefore(startHour)) {
            oneLineLogger.print("Skip, not yet in work hours:",".");
            log.debug("Skipping run, not yet in work hours ");
            return true;
        } else if (now.isAfter(finishHour)) {
            log.info("App will shut down, since it is after working hours... ");
            System.exit(0);
        }
        return false;
    }

    public void postpone(int seconds, String text) {
        lock.lock();
        try {
            futureInstant = System.currentTimeMillis() + (seconds * 1000L);
            log.info("Delay next run for {} seconds. {}", seconds, text);
        } finally {
            lock.unlock();
        }
    }

    public void postponeForTooManyRequests( String text) {
        postpone(props.getTooManyRequestsWaitingDuration(), text);
    }

    public void postponeForLowInvestorBalance( String text) {
        postpone(props.getLowInvestorBalanceWaitingDuration(), text);
    }
}


