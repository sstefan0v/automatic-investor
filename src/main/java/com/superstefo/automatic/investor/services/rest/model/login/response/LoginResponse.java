package com.superstefo.automatic.investor.services.rest.model.login.response;

import lombok.Data;

@Data
public class LoginResponse{
    private String access_token;
    private int expires_in;
    private String refresh_token;
    private String status;
    private String clientId;
    private boolean phoneConfirmed;
    private String verificationStatus;
}