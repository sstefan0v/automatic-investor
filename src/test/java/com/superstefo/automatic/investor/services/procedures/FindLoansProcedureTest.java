package com.superstefo.automatic.investor.services.procedures;

import com.superstefo.automatic.investor.config.InvestProps;
import com.superstefo.automatic.investor.services.JobScheduler;
import com.superstefo.automatic.investor.services.NextProcedureSelector;
import com.superstefo.automatic.investor.services.StateTypes;
import com.superstefo.automatic.investor.services.WalletService;
import com.superstefo.automatic.investor.services.rest.RestAPIService;
import com.superstefo.automatic.investor.services.rest.model.get.loans.AllLoans;
import com.superstefo.automatic.investor.services.rest.model.get.loans.Loan;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindLoansProcedureTest {

    @Mock
    private JobScheduler jobSchedulerMock;

    @Mock
    @SuppressWarnings("unused")
    private InvestProps investPropsMock;

    @Mock
    private WalletService walletServiceMock;

    @Mock
    private RestAPIService restAPIServiceMock;

    @Mock
    private NextProcedureSelector nextProcedureSelector;

    @InjectMocks
    private FindLoansProcedure findLoansProcedure;

    @Captor
    private ArgumentCaptor<BigDecimal> investmentMoneyArgumentCaptor;

    @Captor
    private ArgumentCaptor<Loan> loanArgumentCaptor;

    @Test
    void willInvestInALoanSuccessfully() {
        when(walletServiceMock.approveLoanMoney(any()))
                .thenReturn(BigDecimal.TEN);
        when(walletServiceMock.getInvestorsFreeMoney())
                .thenReturn(BigDecimal.TEN);
        when(restAPIServiceMock.getAvailableLoans())
                .thenReturn(getAllLoans(newLoan(1234)));
        when(restAPIServiceMock.invest(any(), any()))
                .thenReturn((CompletableFuture<StateTypes>) CompletableFuture.completedStage(StateTypes.OK));

        findLoansProcedure.start();

        verify(restAPIServiceMock).invest(investmentMoneyArgumentCaptor.capture(), loanArgumentCaptor.capture());
        assertThat(investmentMoneyArgumentCaptor.getValue(), equalTo(BigDecimal.TEN));

        assertThat(loanArgumentCaptor.getValue().getLoanId(), equalTo(1234));
        verifyNoInteractions(jobSchedulerMock);
    }

    @Test
    void willStartTooManyRequestsProcedure() {
        when(walletServiceMock.approveLoanMoney(any()))
                .thenReturn(BigDecimal.TEN);
        when(walletServiceMock.getInvestorsFreeMoney())
                .thenReturn(BigDecimal.TEN);
        when(restAPIServiceMock.getAvailableLoans())
                .thenReturn(getAllLoans(newLoan(1234)))
                .thenReturn(getAllLoans(newLoan(5678)));
        when(restAPIServiceMock.invest(any(), any()))
                .thenReturn((CompletableFuture<StateTypes>) CompletableFuture.completedStage(StateTypes.OK))
                .thenReturn((CompletableFuture<StateTypes>) CompletableFuture.completedStage(StateTypes.TOO_MANY_REQUESTS));

        simulateScheduledStart(2);

        verify(nextProcedureSelector, times(1)).tooManyRequestsProcedure();
    }

    @Test
    void willStartLowInvestmentBalanceProcedure() {
        when(walletServiceMock.getInvestorsFreeMoney()).thenReturn(BigDecimal.ONE);

        simulateScheduledStart(1);

        verify(nextProcedureSelector, times(1)).lowInvestorBalanceProcedure();
    }

    @ParameterizedTest
    @EnumSource(value = StateTypes.class, names = {"OK", "SERVER_ERROR", "LOAN_LESS_THAN_MIN", "LOAN_SOLD"})
    void willInvestJustOnceIfResultIs(StateTypes types) {
        when(walletServiceMock.approveLoanMoney(any()))
                .thenReturn(BigDecimal.TEN);
        when(walletServiceMock.getInvestorsFreeMoney())
                .thenReturn(BigDecimal.TEN);
        when(restAPIServiceMock.getAvailableLoans())
                .thenReturn(getAllLoans(newLoan(1234)))
                .thenReturn(getAllLoans(newLoan(1234)));
        when(restAPIServiceMock.invest(any(), any()))
                .thenReturn((CompletableFuture<StateTypes>) CompletableFuture.completedStage(types));

        simulateScheduledStart(3);

        verify(restAPIServiceMock, times(1))
                .invest(investmentMoneyArgumentCaptor.capture(), loanArgumentCaptor.capture());
    }

    private void simulateScheduledStart(int invocationsCount) {
        IntStream.rangeClosed(1, invocationsCount).forEach(_ -> findLoansProcedure.start());
    }

    private Loan newLoan(int loanId) {
        Loan loan = new Loan();
        loan.setLoanId(loanId);
        loan.setAllowedToInvest(true);
        loan.setCountry("Bulgaria");
        loan.setInterestRate(9.5);
        loan.setTermType("short");
        loan.setAvailableToInvest(BigDecimal.valueOf(54.8));
        return loan;
    }

    private AllLoans getAllLoans(Loan... data) {
        AllLoans allLoans = new AllLoans();
        allLoans.setTotal(1);
        allLoans.setData(List.of(data));
        return allLoans;
    }
}