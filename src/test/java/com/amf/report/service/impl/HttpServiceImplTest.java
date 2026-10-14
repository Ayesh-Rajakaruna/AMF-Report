package com.amf.report.service.impl;

import com.amf.report.dto.LoyaltyEarnDto;
import com.amf.report.exception.HttpServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HttpServiceImplTest {

    private static final String URL = "https://example.test/tmf-api/loyalty/v4/loyaltyEarn";

    private MockRestServiceServer server;
    private HttpServiceImpl httpService;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        httpService = new HttpServiceImpl(restTemplate);
    }

    @Test
    void get_parsesJsonListAndSendsAcceptHeader() {
        String json = """
                [
                  {
                    "id": "LE-1",
                    "@type": "LoyaltyEarn",
                    "quantity": { "value": 25.50, "unit": "AUD" },
                    "loyaltyProgramMember": { "id": "LPM-2001", "href": "x" },
                    "usageCharacteristic": [
                      { "id": "c1", "name": "EarningType", "valueType": "String",
                        "value": "Monthly", "@type": "StringCharacteristic" },
                      { "id": "c2", "name": "PolicyId", "valueType": "String",
                        "value": "POL-1001", "@type": "StringCharacteristic" }
                    ]
                  }
                ]
                """;
        server.expect(requestTo(URL))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<LoyaltyEarnDto> result =
                httpService.get(URL, new ParameterizedTypeReference<List<LoyaltyEarnDto>>() {});

        server.verify();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQuantity().getValue()).isEqualByComparingTo("25.50");
        assertThat(result.get(0).getUsageCharacteristicValue("EarningType")).isEqualTo("Monthly");
        assertThat(result.get(0).getUsageCharacteristicValue("PolicyId")).isEqualTo("POL-1001");
        assertThat(result.get(0).getLoyaltyProgramMember().getId()).isEqualTo("LPM-2001");
    }

    @Test
    void get_withCustomHeaders_forwardsThem() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token-123");
        server.expect(requestTo(URL))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer token-123"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        List<LoyaltyEarnDto> result =
                httpService.get(URL, headers, new ParameterizedTypeReference<List<LoyaltyEarnDto>>() {});

        server.verify();
        assertThat(result).isEmpty();
    }

    @Test
    void post_sendsJsonBody() {
        server.expect(requestTo(URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"id\":\"LE-9\"}"))
                .andRespond(withSuccess("{\"id\":\"LE-9\"}", MediaType.APPLICATION_JSON));

        LoyaltyEarnDto body = LoyaltyEarnDto.builder().id("LE-9").build();
        LoyaltyEarnDto result = httpService.post(URL, body, null,
                new ParameterizedTypeReference<LoyaltyEarnDto>() {});

        server.verify();
        assertThat(result.getId()).isEqualTo("LE-9");
    }

    @Test
    void get_serverError_throwsHttpServiceException() {
        server.expect(requestTo(URL)).andRespond(withServerError());

        assertThatThrownBy(() -> httpService.get(URL, String.class))
                .isInstanceOf(HttpServiceException.class)
                .hasMessageContaining(URL);
    }
}
