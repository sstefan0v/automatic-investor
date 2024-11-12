package com.sstefanov.automatic.investor.services;

import com.sstefanov.automatic.investor.config.InvestProps;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CycleDelayServiceTest {

    @Mock
    private InvestProps propsMock;

    @InjectMocks
    private CycleDelayService cycleDelayService;

    @Test
    void willWaitDueToRunningBeforeWorkHours() {
        when(propsMock.getWorkCyclesStartHour()).thenReturn(LocalTime.now().plus(Duration.ofHours(1L)));
        cycleDelayService = new CycleDelayService(propsMock);

        assertTrue(cycleDelayService.isToWaitDueToOutOfWorkHours());
    }

    @Test
    void willNotWaitDueToRunningBeforeWorkHours() {
        when(propsMock.getWorkCyclesStartHour()).thenReturn(LocalTime.now().minus(Duration.ofMinutes(1L)));
        when(propsMock.getWorkCyclesFinishHour()).thenReturn(LocalTime.now().plus(Duration.ofHours(1L)));
        cycleDelayService = new CycleDelayService(propsMock);

        assertFalse(cycleDelayService.isToWaitDueToOutOfWorkHours());
    }

    @Test
    void willWaitDueToPostponing() {
        when(propsMock.getTooManyRequestsWaitingDuration()).thenReturn(10);
        when(propsMock.getLowInvestorBalanceWaitingDuration()).thenReturn(10);

        cycleDelayService.postponeForTooManyRequests("many requests");
        cycleDelayService.postponeForLowInvestorBalance("low balance");
        assertTrue(cycleDelayService.isToWaitDueToPostponing());
    }

    @Test
    void willNotWaitDueToPostponing() {
        assertFalse(cycleDelayService.isToWaitDueToPostponing());
    }
}