package com.amf.report.service.impl;

import com.amf.report.dto.LoyaltyEarnDto;
import com.amf.report.dto.LoyaltyEarnResponseDto;
import com.amf.report.entity.LoyaltyEarnRecord;
import com.amf.report.repository.LoyaltyEarnRecordRepository;
import com.amf.report.service.HttpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoyaltyEarnServiceImplTest {

    @Mock
    private LoyaltyEarnRecordRepository repository;

    private static final String LOYALTY_EARN_URL = "https://example.test/loyaltyEarn";

    @Mock
    private HttpService httpService;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private LoyaltyEarnServiceImpl service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "loyaltyEarnUrl", LOYALTY_EARN_URL);
    }

    private void apiReturns(List<LoyaltyEarnDto> loyaltyEarns) {
        when(httpService.get(eq(LOYALTY_EARN_URL), any(ParameterizedTypeReference.class)))
                .thenReturn(loyaltyEarns);
    }

    private static LoyaltyEarnDto loyaltyEarn(String value, String policyId, String memberId) {
        return LoyaltyEarnDto.builder()
                .quantity(new LoyaltyEarnDto.QuantityDto(new BigDecimal(value)))
                .policyId(policyId)
                .loyaltyProgramMember(new LoyaltyEarnDto.LoyaltyProgramMemberDto(memberId))
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<LoyaltyEarnRecord> captureSaved() {
        ArgumentCaptor<List<LoyaltyEarnRecord>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(captor.capture());
        return captor.getValue();
    }

    @Test
    void syncLoyaltyEarnsFromApi_mapsAndSavesEveryRecord() {
        apiReturns(List.of(
                loyaltyEarn("10", "POL-1", "LPM-1"),
                loyaltyEarn("20.75", "POL-2", "LPM-2")));
        when(repository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<LoyaltyEarnResponseDto> result = service.syncLoyaltyEarnsFromApi();

        List<LoyaltyEarnRecord> saved = captureSaved();
        assertThat(saved).hasSize(2);
        assertThat(saved.get(1).getPlanContribution()).isEqualByComparingTo("20.75");
        assertThat(saved.get(1).getPolicyId()).isEqualTo("POL-2");
        assertThat(saved.get(1).getLoyaltyProgramMember()).isEqualTo("LPM-2");
        assertThat(result).extracting(LoyaltyEarnResponseDto::getPolicyId)
                .containsExactly("POL-1", "POL-2");
    }

    @Test
    void syncLoyaltyEarnsFromApi_nullNestedObjects_savesNulls() {
        apiReturns(List.of(
                LoyaltyEarnDto.builder().policyId("POL-1").build()));
        when(repository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<LoyaltyEarnResponseDto> result = service.syncLoyaltyEarnsFromApi();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPlanContribution()).isNull();
        assertThat(result.get(0).getLoyaltyProgramMember()).isNull();
        assertThat(result.get(0).getPolicyId()).isEqualTo("POL-1");
    }

    @Test
    void syncLoyaltyEarnsFromApi_emptyApiResponse_savesNothing() {
        apiReturns(Collections.emptyList());
        when(repository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.syncLoyaltyEarnsFromApi()).isEmpty();
        assertThat(captureSaved()).isEmpty();
    }

    @Test
    void syncLoyaltyEarnsFromApi_nullApiBody_savesNothing() {
        apiReturns(null);
        when(repository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.syncLoyaltyEarnsFromApi()).isEmpty();
    }
}
