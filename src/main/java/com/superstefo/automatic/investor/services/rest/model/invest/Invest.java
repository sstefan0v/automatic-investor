package com.superstefo.automatic.investor.services.rest.model.invest;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Invest {
    private final String amount;
    private Invest(String amount) {
        this.amount = amount;
    }

    public static Invest getNew(String val){
        return new Invest(val);
    }
}
