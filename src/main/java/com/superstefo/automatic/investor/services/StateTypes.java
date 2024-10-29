package com.superstefo.automatic.investor.services;

import java.util.Objects;

public enum StateTypes {
    NA(0, "N/A"),
    OK(200, "OK"),
    LOW_BALANCE(4001, "Low investor balance"),
    LOAN_LESS_THAN_MIN(4002, "The loan available amount is less than"),
    FREQUENT_REQUESTS(4003, "Request is too frequent,"),
    INVESTMENT_LESS_THAN_MIN(4004, "The minimal amount is 10"),
    LOAN_SOLD(4005, "The loan is sold out"),
    TOO_MANY_REQUESTS(429, "Too many requests"),
    SERVER_ERROR(500, "server error....");
    public final int code;
    public final String info;

    StateTypes(int code, String info) {
        this.code = code;
        this.info = info;

    }

    public static StateTypes fromErrorMessage(RuntimeException e) {
        String msg = Objects.requireNonNullElse(e.getMessage(), "");
        for (StateTypes type : StateTypes.values()) {
            if (msg.contains(type.info)) {
                return type;
            }
        }
        return StateTypes.NA;
    }

}
