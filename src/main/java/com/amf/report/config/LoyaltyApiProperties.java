package com.amf.report.config;

import com.amf.report.constant.ReportEnvironment;
import com.amf.report.exception.InvalidRequestException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.Map;

@ConfigurationProperties(prefix = "loyalty.api")
public record LoyaltyApiProperties(
        Map<ReportEnvironment, String> baseUrls,
        String loyaltyEarnPath,
        String loyaltyRulePath,
        String loyaltyProgramMemberPath,
        String loyaltyAccountBalancePath) {

    public LoyaltyApiProperties {
        baseUrls = baseUrls == null ? Map.of() : Map.copyOf(baseUrls);
    }

    public String loyaltyEarnUrl(ReportEnvironment environment) {
        return baseUrl(environment) + loyaltyEarnPath;
    }

    public String loyaltyRuleUrl(ReportEnvironment environment) {
        return baseUrl(environment) + loyaltyRulePath;
    }

    public String loyaltyProgramMemberUrl(ReportEnvironment environment) {
        return baseUrl(environment) + loyaltyProgramMemberPath;
    }

    public String loyaltyAccountBalanceUrl(ReportEnvironment environment) {
        return baseUrl(environment) + loyaltyAccountBalancePath;
    }

    private String baseUrl(ReportEnvironment environment) {
        String url = baseUrls.get(environment);
        if (!StringUtils.hasText(url)) {
            throw new InvalidRequestException(
                    "No base URL configured for environment " + environment.getLabel()
                            + " (loyalty.api.base-urls." + environment.name() + ")");
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
