package com.superstefo.automatic.investor.services.rest.model.get.loans;

import java.math.BigDecimal;

@lombok.Data
public class Loan {
    private int loanId;
    private int countryId;
    private String country;
    private String countryIso;
    private String loanOriginator;
    private int originatorId;
    private String issuedDate;
    private String finalPaymentDate;
    private String termType;
    private String termTypeTitle;
    private String status;
    private String statusTitle;
    private double interestRate;
    private int remainingTerm;
    private int term;
    private int initialTerm;
    private double loanAmount;
    private double assignedAmount;
    private BigDecimal availableToInvest;
    private boolean allowedToInvest;
    private int minimumInvestmentAmount;
    private double investedAmount;
    private String currencySign;
    private boolean buyback;
    private boolean sellback;
    private int days;
    private int order_position;
}