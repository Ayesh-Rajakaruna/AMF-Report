package com.amf.report.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.security.KeyStore;
import java.time.Duration;

@Slf4j
@Configuration
public class BeanConfig {

    @Value("${http.proxy.enabled:false}")
    private boolean proxyEnabled;

    @Value("${http.proxy.host:}")
    private String proxyHost;

    @Value("${http.proxy.port:8080}")
    private int proxyPort;

    @Value("${http.proxy.username:}")
    private String proxyUsername;

    @Value("${http.proxy.password:}")
    private String proxyPassword;

    @Value("${http.read-timeout-ms:30000}")
    private long readTimeoutMs;

    /** Optional, e.g. file:D:/certs/truststore.jks or classpath:truststore.jks */
    @Value("${trust.store:}")
    private String trustStore;

    @Value("${trust.password:}")
    private String trustStorePassword;

    private final ResourceLoader resourceLoader;

    public BeanConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public RestTemplate restTemplate() throws Exception {
        PoolingHttpClientConnectionManagerBuilder connectionManager =
                PoolingHttpClientConnectionManagerBuilder.create();
        if (StringUtils.hasText(trustStore)) {
            connectionManager.setTlsSocketStrategy(new DefaultClientTlsStrategy(buildSslContext()));
            log.info("RestTemplate using custom trust store: {}", trustStore);
        }

        HttpClientBuilder clientBuilder = HttpClients.custom()
                .setConnectionManager(connectionManager.build())
                .disableCookieManagement();

        if (proxyEnabled && StringUtils.hasText(proxyHost)) {
            HttpHost proxy = new HttpHost(proxyHost, proxyPort);
            clientBuilder.setProxy(proxy);

            if (StringUtils.hasText(proxyUsername)) {
                BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(
                        new AuthScope(proxy),
                        new UsernamePasswordCredentials(proxyUsername, proxyPassword.toCharArray()));
                clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
            log.info("RestTemplate using proxy {}:{}", proxyHost, proxyPort);
        }

        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(clientBuilder.build());
        factory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

        return new RestTemplate(factory);
    }

    private SSLContext buildSslContext() throws Exception {
        Resource resource = resourceLoader.getResource(trustStore);
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (InputStream in = resource.getInputStream()) {
            keyStore.load(in, trustStorePassword.toCharArray());
        }
        return SSLContextBuilder.create()
                .loadTrustMaterial(keyStore, null)
                .build();
    }
}
