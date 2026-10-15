package com.amf.report.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;

public interface HttpService {
    <T> T get(String url, Class<T> responseType);

    <T> T get(String url, ParameterizedTypeReference<T> responseType);

    <T> T get(String url, HttpHeaders headers, ParameterizedTypeReference<T> responseType);

    <T> T post(String url, Object body, HttpHeaders headers, ParameterizedTypeReference<T> responseType);
}
