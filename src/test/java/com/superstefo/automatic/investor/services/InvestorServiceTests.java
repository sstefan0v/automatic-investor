package com.superstefo.automatic.investor.services;

import com.superstefo.automatic.investor.config.ThreadPoolConfig;
import com.superstefo.automatic.investor.services.rest.RestAPIService;
import com.superstefo.automatic.investor.services.rest.model.get.loans.AllLoans;
import com.superstefo.automatic.investor.services.rest.model.get.loans.Loan;
import com.superstefo.automatic.investor.services.rest.model.invest.Invest;
import com.superstefo.automatic.investor.services.rest.model.main.info.MainInfo;
import com.superstefo.automatic.investor.services.rest.RestAPIConnector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpMethod.GET;

@ExtendWith(MockitoExtension.class)
class InvestorServiceTests {

//    @Mock
//    private Props props;

    @Mock
    private RestAPIConnector restAPIConnectorMock;

    @Mock
    private RestAPIService restAPIServiceMock;

    @Mock
    private WalletService walletService;

    @Spy
    private JobScheduler scheduler;

    @Spy
    private Executor executor = new ThreadPoolConfig().getThreadPoolTaskExecutor();

    @InjectMocks
    private InvestorService investorService;

    @Captor
    private ArgumentCaptor<HttpEntity> investArgumentCaptor;

    //@Test
    void willInvestInALoanSuccessfully() {
        investorService.setInvestingRestAPIService(restAPIServiceMock);
        when(restAPIConnectorMock.exchange(null, GET, new HttpEntity<>(""), MainInfo.class)).thenReturn(getMainInfo(500));
        when(restAPIConnectorMock.exchange(null, GET, new HttpEntity<>(""), AllLoans.class)).thenReturn(getAllLoans(newLoan()));
        when(walletService.approveLoanMoney(BigDecimal.valueOf(23d))).thenReturn(BigDecimal.valueOf(23d));
        when(walletService.getInvestorsFreeMoney()).thenReturn(BigDecimal.valueOf(231d));

        simulateScheduledStart(2);

        verify(restAPIConnectorMock, timeout(1000)).exchange(anyString(), any(HttpMethod.class),  investArgumentCaptor.capture(), any());
        Invest invest = (Invest) investArgumentCaptor.getValue().getBody();
        assertThat(invest.getAmount(), equalTo("23.0"));
    }

    @Test
    void willPostponeDueToTooManyRequests() {
//        when(props.getWaitingTimeTooManyRequests()).thenReturn(45);
//        when(walletService.getWalletSize()).thenReturn(BigDecimal.valueOf(2231));
//        when(restAPIServiceMock.exchange(null, GET, "", MainInfo.class)).thenReturn(getMainInfo(500));
//        when(restAPIServiceMock.exchange(null, GET, "", AllLoans.class)).thenThrow(
//                HttpClientErrorException.create(HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", null, null, null));
//
//        simulateScheduledStart(3);
//
//        verify(scheduler, timeout(1000)).postpone(45, StateTypes.TOO_MANY_REQUESTS);
    }

    @Test
    void willPostponeDueToLowBalance() {
//        when(props.getWaitingTimeLowInvestorBalance()).thenReturn(25);
//        when(restAPIServiceMock.exchange(null, GET, "", MainInfo.class)).thenReturn(getMainInfo(5));
//
//        simulateScheduledStart(4);
//
//        verify(scheduler, timeout(1000)).postpone(25, StateTypes.LOW_BALANCE);
    }


    private void simulateScheduledStart(int invocationsCount) {
        IntStream.rangeClosed(0, invocationsCount).forEach(i -> scheduler.start());
    }

    private Loan newLoan() {
        Loan loan = new Loan();
        loan.setAllowedToInvest(true);
//        loan.setAvailableToInvest(23d);
        return loan;
    }

    private MainInfo getMainInfo(double sum) {
        MainInfo mainInfo = new MainInfo();
//        mainInfo.setAvailableMoney(sum);
        return mainInfo;
    }

    private AllLoans getAllLoans(Loan... data) {
        AllLoans allLoans = new AllLoans();
        allLoans.setTotal(1);
        allLoans.setData(List.of(data));
        return allLoans;
    }
}
