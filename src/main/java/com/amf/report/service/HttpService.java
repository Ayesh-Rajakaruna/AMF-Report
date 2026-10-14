package com.amf.report.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;

/** Common outbound HTTP client (backed by the RestTemplate bean). */
public interface HttpService {

    /** GET {@code url} and read the JSON body as {@code responseType}. */
    <T> T get(String url, Class<T> responseType);

    /** GET {@code url} for generic types, e.g. {@code new ParameterizedTypeReference<List<Foo>>() {}}. */
    <T> T get(String url, ParameterizedTypeReference<T> responseType);

    /** GET {@code url} with extra headers (auth tokens, correlation ids, ...). */
    <T> T get(String url, HttpHeaders headers, ParameterizedTypeReference<T> responseType);

    /** POST {@code body} as JSON to {@code url} and read the JSON response as {@code responseType}. */
    <T> T post(String url, Object body, HttpHeaders headers, ParameterizedTypeReference<T> responseType);
}
