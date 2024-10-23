package com.superstefo.automatic.investor.services.rest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;

/**
 * Intended for single use, to save time before each request. Call it once at application start
 * and use object at Service level
 */
final class HttpHeaderUtils {

     static MultiValueMap<String, String> getAuthHeaders(String authToken) {
        MultiValueMap h  = getLoginHeaders();
        h.add("Authorization", authToken);
         return h;
    }

     static MultiValueMap<String, String> getLoginHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.add("Referer", "https://tt.com/");
        h.add("sec-ch-ua", "\"Not A(Brand\";v=\"99\", \"Microsoft Edge\";v=\"121\", \"Chromium\";v=\"121\"");
        h.add("sec-ch-ua-mobile", "?0");
        h.add("sec-ch-ua-platform", "\"Windows\"");
        h.add("Sec-Fetch-Dest", "empty");
        h.add("Sec-Fetch-Mode", "cors");
        h.add("Sec-Fetch-Site", "same-origin");
        h.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36 Edg/121.0.0.0");
        h.add("X-App-Build-Version", "1.0.1");
        h.add("X-App-System", "Windows 10");
        h.add("X-App-User-Agent", "Edge 121.0.0.0");
        return h;
    }
}
