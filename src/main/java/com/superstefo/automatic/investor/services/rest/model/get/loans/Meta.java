package com.superstefo.automatic.investor.services.rest.model.get.loans;

import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.Data
public class Meta{
    private int pages;
    @JsonProperty( "per-page")
    private int perPage;

    private String sort;
    private Direction direction;
    private int offset;
    private int pageSize;
}