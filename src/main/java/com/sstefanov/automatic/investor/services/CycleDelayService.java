package com.sstefanov.automatic.investor.services;

import com.sstefanov.automatic.investor.config.InvestProps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.sstefanov.automatic.investor.config.Constants.SHUT_DOWN_AFTER_WORK_HOURS;
import static com.sstefanov.automatic.investor.config.Constants.SKIP_RUN_BEFORE_WORK_HOURS;

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

    public boolean isToWaitDueToPostponing() {
        lock.lock();
        try {
            return futureInstant > System.currentTimeMillis();
        } finally {
            lock.unlock();
        }
    }

    public boolean isToWaitDueToOutOfWorkHours() {
        LocalTime now = LocalTime.now();
        if (now.isBefore(startHour)) {
            log.debug(SKIP_RUN_BEFORE_WORK_HOURS);
            oneLineLogger.print(SKIP_RUN_BEFORE_WORK_HOURS, ".");
            return true;
        } else if (now.isAfter(finishHour)) {
            log.info(SHUT_DOWN_AFTER_WORK_HOURS);
            System.exit(0);
        }
        return false;
    }

    private void postpone(int seconds, String text) {
        lock.lock();
        try {
            futureInstant = System.currentTimeMillis() + (seconds * 1000L);
            log.info("Delay next run for {} seconds. {}", seconds, text);
        } finally {
            lock.unlock();
        }
    }

    public void postponeForTooManyRequests(String text) {
        postpone(props.getTooManyRequestsWaitingDuration(), text);
    }

    public void postponeForLowInvestorBalance(String text) {
        postpone(props.getLowInvestorBalanceWaitingDuration(), text);
    }
}


