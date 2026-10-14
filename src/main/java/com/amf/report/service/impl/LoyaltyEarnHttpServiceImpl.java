package com.amf.report.service.impl;

import com.amf.report.dto.LoyaltyEarnDto;
import com.amf.report.exception.LoyaltyEarnApiException;
import com.amf.report.service.LoyaltyEarnHttpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class LoyaltyEarnHttpServiceImpl implements LoyaltyEarnHttpService {

    private static final ParameterizedTypeReference<List<LoyaltyEarnDto>> LOYALTY_EARN_LIST =
            new ParameterizedTypeReference<>() {};

    private final RestTemplate restTemplate;
    private final String loyaltyEarnUrl;

    public LoyaltyEarnHttpServiceImpl(RestTemplate restTemplate,
                                      @Value("${loyalty.api.loyalty-earn-url}") String loyaltyEarnUrl) {
        this.restTemplate = restTemplate;
        this.loyaltyEarnUrl = loyaltyEarnUrl;
    }

    @Override
    public List<LoyaltyEarnDto> getAllLoyaltyEarns() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        log.info("Calling loyaltyEarn API: GET {}", loyaltyEarnUrl);
        try {
            ResponseEntity<List<LoyaltyEarnDto>> response = restTemplate.exchange(
                    loyaltyEarnUrl, HttpMethod.GET, new HttpEntity<>(headers), LOYALTY_EARN_LIST);

            List<LoyaltyEarnDto> body = response.getBody();
            log.info("loyaltyEarn API returned {} record(s), status {}",
                    body == null ? 0 : body.size(), response.getStatusCode());
            return body == null ? Collections.emptyList() : body;
        } catch (RestClientException ex) {
            throw new LoyaltyEarnApiException(
                    "Failed to call loyaltyEarn API " + loyaltyEarnUrl + ": " + ex.getMessage(), ex);
        }
    }
}
