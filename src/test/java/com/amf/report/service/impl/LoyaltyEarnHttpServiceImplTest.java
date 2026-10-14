package com.amf.report.service.impl;

import com.amf.report.dto.LoyaltyEarnDto;
import com.amf.report.exception.LoyaltyEarnApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class LoyaltyEarnHttpServiceImplTest {

    private static final String URL = "https://example.test/tmf-api/loyalty/v4/loyaltyEarn";

    private MockRestServiceServer server;
    private LoyaltyEarnHttpServiceImpl httpService;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        httpService = new LoyaltyEarnHttpServiceImpl(restTemplate, URL);
    }

    @Test
    void getAllLoyaltyEarns_parsesTmfResponse() {
        String json = """
                [
                  {
                    "id": "LE-1",
                    "@type": "LoyaltyEarn",
                    "quantity": { "value": 25.50, "unit": "points" },
                    "policyId": "POL-1001",
                    "loyaltyProgramMember": { "id": "LPM-2001", "href": "x" }
                  }
                ]
                """;
        server.expect(requestTo(URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<LoyaltyEarnDto> result = httpService.getAllLoyaltyEarns();

        server.verify();
        assertThat(result).hasSize(1);
        LoyaltyEarnDto earn = result.get(0);
        assertThat(earn.getQuantity().getValue()).isEqualByComparingTo("25.50");
        assertThat(earn.getPolicyId()).isEqualTo("POL-1001");
        assertThat(earn.getLoyaltyProgramMember().getId()).isEqualTo("LPM-2001");
    }

    @Test
    void getAllLoyaltyEarns_serverError_throwsApiException() {
        server.expect(requestTo(URL)).andRespond(withServerError());

        assertThatThrownBy(() -> httpService.getAllLoyaltyEarns())
                .isInstanceOf(LoyaltyEarnApiException.class);
    }
}
