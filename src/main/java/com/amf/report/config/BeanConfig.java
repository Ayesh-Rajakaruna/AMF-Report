package com.amf.report.config;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import static com.amf.report.constant.AppConstants.PROXY_HOST;
import static com.amf.report.constant.AppConstants.PROXY_PORT;
import static com.amf.report.constant.AppConstants.PROXY_USERNAME;

@Configuration
public class BeanConfig {

    /** Proxy password comes from the HTTP_PROXY_PASSWORD environment variable - never hardcode it. */
    @Value("${http.proxy.password}")
    private String proxyPassword;

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public RestTemplate restTemplate() {
        BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(PROXY_HOST, PROXY_PORT),
                new UsernamePasswordCredentials(PROXY_USERNAME, proxyPassword.toCharArray())
        );
        HttpHost myProxy = new HttpHost(PROXY_HOST, PROXY_PORT);
        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        clientBuilder.setProxy(myProxy).setDefaultCredentialsProvider(credsProvider).disableCookieManagement();
        HttpClient httpClient = clientBuilder.build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(httpClient);
        return new RestTemplate(factory);
    }
}
