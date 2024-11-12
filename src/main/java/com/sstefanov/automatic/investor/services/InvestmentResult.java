package com.sstefanov.automatic.investor.services;

import java.util.Objects;

public enum InvestmentResult {
    NA("N/A"),
    OK("OK"),
    LOW_BALANCE("Low investor balance"),
    LOAN_LESS_THAN_MIN("The loan available amount is less than"),
    FREQUENT_REQUESTS("Request is too frequent,"),
    INVESTMENT_LESS_THAN_MIN("The minimal amount is 10"),
    LOAN_SOLD("The loan is sold out"),
    TOO_MANY_REQUESTS("Too many requests"),
    SERVER_ERROR("server error....");

    public final String info;

    InvestmentResult(String info) {
        this.info = info;
    }

    public static InvestmentResult fromErrorMessage(RuntimeException e) {
        String msg = Objects.requireNonNullElse(e.getMessage(), "");
        for (InvestmentResult type : InvestmentResult.values()) {
            if (msg.contains(type.info)) {
                return type;
            }
        }
        return InvestmentResult.NA;
    }
}
