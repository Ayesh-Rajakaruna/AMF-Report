package com.amf.report.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.util.Timeout;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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

@Slf4j
@Configuration
@EnableConfigurationProperties(ProductGroupApiProperties.class)
public class BeanConfig {
    @Value("${http.proxy.enabled:true}")
    private boolean proxyEnabled;

    @Value("${http.proxy.scheme:http}")
    private String proxyScheme;

    @Value("${http.proxy.host:}")
    private String proxyHost;

    @Value("${http.proxy.port:8080}")
    private int proxyPort;

    @Value("${http.proxy.username:}")
    private String proxyUsername;

    @Value("${http.proxy.password:}")
    private String proxyPassword;

    @Value("${http.timeout.connect-seconds:30}")
    private long connectTimeoutSeconds;

    @Value("${http.timeout.read-seconds:60}")
    private long readTimeoutSeconds;

    @Value("${trust.store:}")
    private String trustStore;

    @Value("${trust.password:}")
    private String trustStorePassword;

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean(destroyMethod = "close")
    public CloseableHttpClient httpClient(ResourceLoader resourceLoader) {
        PoolingHttpClientConnectionManagerBuilder connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultConnectionConfig(ConnectionConfig.custom()
                        .setConnectTimeout(Timeout.ofSeconds(connectTimeoutSeconds))
                        .setSocketTimeout(Timeout.ofSeconds(readTimeoutSeconds))
                        .build());

        SSLContext sslContext = buildSslContext(resourceLoader);
        if (sslContext != null) {
            connectionManager.setTlsSocketStrategy(new DefaultClientTlsStrategy(sslContext));
        }

        HttpClientBuilder clientBuilder = HttpClients.custom()
                .setConnectionManager(connectionManager.build())
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setResponseTimeout(Timeout.ofSeconds(readTimeoutSeconds))
                        .build())
                .disableCookieManagement();

        if (proxyEnabled && StringUtils.hasText(proxyHost)) {
            HttpHost proxy = new HttpHost(proxyScheme, proxyHost, proxyPort);
            clientBuilder.setProxy(proxy);

            if (StringUtils.hasText(proxyUsername)) {
                if (!StringUtils.hasText(proxyPassword)) {
                    log.warn("Proxy user name is set but http.proxy.password is empty. Set the PROXY_PASSWORD environment variable.");
                }
                BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(
                        new AuthScope(proxy),
                        new UsernamePasswordCredentials(proxyUsername, proxyPassword.toCharArray()));
                clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
            log.info("Outbound HTTP calls use proxy {}://{}:{} (user: {})",
                    proxyScheme, proxyHost, proxyPort, StringUtils.hasText(proxyUsername) ? proxyUsername : "<none>");
        } else {
            log.info("Outbound HTTP calls go direct (no proxy)");
        }

        return clientBuilder.build();
    }

    @Bean
    public RestTemplate restTemplate(CloseableHttpClient httpClient) {
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
    }

    private SSLContext buildSslContext(ResourceLoader resourceLoader) {
        if (!StringUtils.hasText(trustStore)) {
            return null;
        }
        Resource resource = resourceLoader.getResource(trustStore);
        try (InputStream in = resource.getInputStream()) {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(in, trustStorePassword.toCharArray());
            log.info("Using trust store {}", trustStore);
            return SSLContextBuilder.create()
                    .loadTrustMaterial(keyStore, null)
                    .build();
        } catch (Exception ex) {
            throw new IllegalStateException("Could not load trust store '" + trustStore + "': " + ex.getMessage(), ex);
        }
    }
}
