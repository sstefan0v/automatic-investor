package com.sstefanov.automatic.investor.services.rest.model.get.loans;

@lombok.Data
public class Sort{
    private String loanId;
    private String countryId;
    private String loanOriginator;
    private String issuedDate;
    private String termTypeTitle;
    private String interestRate;
    private String term;
    private String availableToInvest;
}