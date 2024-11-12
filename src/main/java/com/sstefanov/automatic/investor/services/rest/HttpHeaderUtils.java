package com.sstefanov.automatic.investor.services.rest;

import com.sstefanov.automatic.investor.config.InvestProps;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;

/**
 * Intended for single use, to save time before each request. Call it once at application start
 * and use object at Service level
 */
final class HttpHeaderUtils {

     static MultiValueMap<String, String> getAuthHeaders(InvestProps props, String authToken) {
        MultiValueMap<String, String> headers  = getLoginHeaders(props);
        headers.add("Authorization", authToken);
         return headers;
    }

     static MultiValueMap<String, String> getLoginHeaders(InvestProps props) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Referer", props.getBaseUrl());
        headers.add("sec-ch-ua", "\"Not A(Brand\";v=\"99\", \"Microsoft Edge\";v=\"121\", \"Chromium\";v=\"121\"");
        headers.add("sec-ch-ua-mobile", "?0");
        headers.add("sec-ch-ua-platform", "\"Windows\"");
        headers.add("Sec-Fetch-Dest", "empty");
        headers.add("Sec-Fetch-Mode", "cors");
        headers.add("Sec-Fetch-Site", "same-origin");
        headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36 Edg/121.0.0.0");
        headers.add("X-App-Build-Version", "1.0.1");
        headers.add("X-App-System", "Windows 10");
        headers.add("X-App-User-Agent", "Edge 121.0.0.0");
        return headers;
    }
}
