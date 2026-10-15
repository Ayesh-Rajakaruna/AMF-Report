package com.amf.report.config;

import com.amf.report.constant.ReportEnvironment;
import com.amf.report.exception.InvalidRequestException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * Product group / organization API settings from application.yml (product-group.api.*).
 */
@ConfigurationProperties(prefix = "product-group.api")
public record ProductGroupApiProperties(
        Map<ReportEnvironment, String> baseUrls,
        String productGroupPath,
        String organizationPath,
        int organizationChunkThreshold,
        int organizationChunkSize) {

    private static final int DEFAULT_CHUNK_THRESHOLD = 30;
    private static final int DEFAULT_CHUNK_SIZE = 25;

    public ProductGroupApiProperties {
        baseUrls = baseUrls == null ? Map.of() : Map.copyOf(baseUrls);
        if (organizationChunkThreshold <= 0) {
            organizationChunkThreshold = DEFAULT_CHUNK_THRESHOLD;
        }
        if (organizationChunkSize <= 0) {
            organizationChunkSize = DEFAULT_CHUNK_SIZE;
        }
    }

    public String baseUrl(ReportEnvironment environment) {
        String url = baseUrls.get(environment);
        if (!StringUtils.hasText(url)) {
            throw new InvalidRequestException(
                    "No base URL configured for environment " + environment.getLabel()
                            + " (product-group.api.base-urls." + environment.name() + ")");
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
