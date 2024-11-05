package com.superstefo.automatic.investor.services.rest;

import com.superstefo.automatic.investor.config.InvestProps;
import com.superstefo.automatic.investor.services.rest.model.login.response.LoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

import static com.superstefo.automatic.investor.config.InvestProps.LOGIN_URL;
import static com.superstefo.automatic.investor.services.rest.HttpHeaderUtils.getLoginHeaders;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Service
@Slf4j
@RequiredArgsConstructor
public abstract class RestAPIConnector {

    private final RestTemplate restTemplate = new RestTemplate();
    private final InvestProps investProps;

    <R,T> R exchange(String url, HttpMethod method, HttpEntity<T> httpEntity, Class<R> clazz) {
        url = investProps.getBaseUrl() + url;
        log.debug("Preparing request url={}, body={}", url, httpEntity.getBody());
        try {
            return restTemplate.exchange(url, method, httpEntity, clazz).getBody();
        } catch (RuntimeException e) {
            whatToDoNextBasedOnException(e);
        }
        throw new RuntimeException();
    }

    String getAuthTokenFromEndpoint(String email, String password) {
        String url = investProps.getBaseUrl() + LOGIN_URL;
        log.debug("Preparing request, sending credentials to url={}, email={}", url, email);
        try {
            ResponseEntity<LoginResponse> loginResponse = restTemplate.exchange(
                    url,
                    POST,
                    new HttpEntity<>(getLoginBody(email, password), getLoginHeaders(investProps)),
                    LoginResponse.class);
            return Objects.requireNonNull(loginResponse.getBody()).getAccess_token();
        } catch (RuntimeException e) {
            whatToDoNextBasedOnException(e);
        }
        throw new RuntimeException();
    }

    void confirmHostReachable() {
        log.debug("Preparing request, confirming host is reachable...{}", investProps.getBaseUrl());
        try {
            restTemplate.exchange(
                    investProps.getBaseUrl(),
                    GET,
                    new HttpEntity<>(null, getLoginHeaders(investProps)),
                    String.class);

        } catch (RuntimeException e) {
            whatToDoNextBasedOnException(e);
        }
    }

    private String getLoginBody(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    private void whatToDoNextBasedOnException(RuntimeException exc) {
        switch (exc) {
            case HttpClientErrorException.Unauthorized e:
                stopApp(e);
                break;
            case HttpClientErrorException.Forbidden e:
                stopApp(e);
                break;
            default:
                throw exc;
        }
    }

    private void stopApp(HttpClientErrorException e) {
        log.error("Application shutting down!  {}; {}", e.getStatusCode(), e.getMessage());
        System.exit(1);
    }
}
