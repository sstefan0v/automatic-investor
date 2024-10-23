package com.superstefo.automatic.investor.services.rest.model.get.loans;

import java.util.List;

@lombok.Data
public class AllLoans {
    private Sort sort;
    private List<Loan> data;
    private int total;
    private Meta _meta;
}
