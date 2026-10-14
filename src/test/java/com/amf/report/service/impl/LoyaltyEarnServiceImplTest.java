package com.amf.report.service.impl;

import com.amf.report.dto.LoyaltyEarnDto;
import com.amf.report.dto.LoyaltyEarnDto.CharacteristicDto;
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
import java.util.ArrayList;
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

    private static final String LOYALTY_EARN_URL = "https://example.test/loyaltyEarn";

    @Mock
    private LoyaltyEarnRecordRepository repository;

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

    private void saveAllReturnsInput() {
        when(repository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
    }

    private static LoyaltyEarnDto loyaltyEarn(String value, String earningType, String policyId, String memberId) {
        List<CharacteristicDto> characteristics = new ArrayList<>();
        characteristics.add(new CharacteristicDto("AddedDate", "21-09-2026"));
        if (earningType != null) {
            characteristics.add(new CharacteristicDto("EarningType", earningType));
        }
        if (policyId != null) {
            characteristics.add(new CharacteristicDto("PolicyId", policyId));
        }
        return LoyaltyEarnDto.builder()
                .quantity(value == null ? null : new LoyaltyEarnDto.QuantityDto(new BigDecimal(value)))
                .loyaltyProgramMember(memberId == null ? null : new LoyaltyEarnDto.LoyaltyProgramMemberDto(memberId))
                .usageCharacteristic(characteristics)
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<LoyaltyEarnRecord> captureSaved() {
        ArgumentCaptor<List<LoyaltyEarnRecord>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(captor.capture());
        return captor.getValue();
    }

    @Test
    void sync_mapsAllFieldsIncludingEarningTypeAndPolicyIdFromCharacteristics() {
        apiReturns(List.of(loyaltyEarn("13.825570", "Monthly", "POL-1", "LPM-1")));
        saveAllReturnsInput();

        List<LoyaltyEarnResponseDto> result = service.syncLoyaltyEarnsFromApi();

        LoyaltyEarnRecord saved = captureSaved().get(0);
        assertThat(saved.getPlanContribution()).isEqualByComparingTo("13.825570");
        assertThat(saved.getPolicyId()).isEqualTo("POL-1");
        assertThat(saved.getLoyaltyProgramMember()).isEqualTo("LPM-1");
        assertThat(saved.getEarningType()).isEqualTo("Monthly");
        assertThat(result.get(0).getEarningType()).isEqualTo("Monthly");
    }

    @Test
    void sync_keepsOnlyMonthlyAndUpfront() {
        apiReturns(List.of(
                loyaltyEarn("10", "Monthly", "POL-1", "LPM-1"),
                loyaltyEarn("20", "Upfront", "POL-2", "LPM-2"),
                loyaltyEarn("30", "ManualCredit", "POL-3", "LPM-3"),
                loyaltyEarn("40", null, "POL-4", "LPM-4")));
        saveAllReturnsInput();

        List<LoyaltyEarnResponseDto> result = service.syncLoyaltyEarnsFromApi();

        assertThat(captureSaved())
                .extracting(LoyaltyEarnRecord::getEarningType)
                .containsExactly("Monthly", "Upfront");
        assertThat(result).extracting(LoyaltyEarnResponseDto::getPolicyId)
                .containsExactly("POL-1", "POL-2");
    }

    @Test
    void sync_earningTypeMatchIsCaseInsensitiveAndStoredInCanonicalCase() {
        apiReturns(List.of(loyaltyEarn("5", " upfront ", "POL-1", "LPM-1")));
        saveAllReturnsInput();

        service.syncLoyaltyEarnsFromApi();

        assertThat(captureSaved().get(0).getEarningType()).isEqualTo("Upfront");
    }

    @Test
    void sync_missingOptionalFields_savesNulls() {
        apiReturns(List.of(loyaltyEarn(null, "Upfront", null, null)));
        saveAllReturnsInput();

        LoyaltyEarnResponseDto result = service.syncLoyaltyEarnsFromApi().get(0);

        assertThat(result.getPlanContribution()).isNull();
        assertThat(result.getPolicyId()).isNull();
        assertThat(result.getLoyaltyProgramMember()).isNull();
        assertThat(result.getEarningType()).isEqualTo("Upfront");
    }

    @Test
    void sync_recordWithoutCharacteristics_isSkipped() {
        apiReturns(List.of(LoyaltyEarnDto.builder().id("LE-1").build()));
        saveAllReturnsInput();

        assertThat(service.syncLoyaltyEarnsFromApi()).isEmpty();
        assertThat(captureSaved()).isEmpty();
    }

    @Test
    void sync_emptyApiResponse_savesNothing() {
        apiReturns(Collections.emptyList());
        saveAllReturnsInput();

        assertThat(service.syncLoyaltyEarnsFromApi()).isEmpty();
        assertThat(captureSaved()).isEmpty();
    }

    @Test
    void sync_nullApiBody_savesNothing() {
        apiReturns(null);
        saveAllReturnsInput();

        assertThat(service.syncLoyaltyEarnsFromApi()).isEmpty();
    }
}
