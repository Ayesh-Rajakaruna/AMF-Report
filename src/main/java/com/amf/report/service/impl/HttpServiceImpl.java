package com.amf.report.service.impl;

import com.amf.report.exception.HttpServiceException;
import com.amf.report.service.HttpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HttpServiceImpl implements HttpService {

    private final RestTemplate restTemplate;

    @Override
    public <T> T get(String url, Class<T> responseType) {
        return get(url, ParameterizedTypeReference.forType(responseType));
    }

    @Override
    public <T> T get(String url, ParameterizedTypeReference<T> responseType) {
        return get(url, new HttpHeaders(), responseType);
    }

    @Override
    public <T> T get(String url, HttpHeaders headers, ParameterizedTypeReference<T> responseType) {
        return exchange(url, HttpMethod.GET, null, headers, responseType);
    }

    @Override
    public <T> T post(String url, Object body, HttpHeaders headers, ParameterizedTypeReference<T> responseType) {
        return exchange(url, HttpMethod.POST, body, headers, responseType);
    }

    private <T> T exchange(String url, HttpMethod method, Object body,
                           HttpHeaders headers, ParameterizedTypeReference<T> responseType) {
        HttpHeaders requestHeaders = new HttpHeaders();
        if (headers != null) {
            requestHeaders.addAll(headers);
        }
        if (requestHeaders.getAccept().isEmpty()) {
            requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        }
        if (body != null && requestHeaders.getContentType() == null) {
            requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        }

        log.info("HTTP {} {}", method, url);
        try {
            ResponseEntity<T> response = restTemplate.exchange(
                    url, method, new HttpEntity<>(body, requestHeaders), responseType);
            log.info("HTTP {} {} -> {}", method, url, response.getStatusCode());
            return response.getBody();
        } catch (RestClientException ex) {
            throw new HttpServiceException(
                    "HTTP " + method + " " + url + " failed: " + ex.getMessage(), ex);
        }
    }
}
