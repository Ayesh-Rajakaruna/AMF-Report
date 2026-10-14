package com.amf.report.service.impl;

import com.amf.report.dto.LoyaltyEarnDto;
import com.amf.report.dto.LoyaltyEarnResponseDto;
import com.amf.report.entity.LoyaltyEarnRecord;
import com.amf.report.repository.LoyaltyEarnRecordRepository;
import com.amf.report.service.LoyaltyEarnHttpService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoyaltyEarnServiceImplTest {

    @Mock
    private LoyaltyEarnRecordRepository repository;

    @Mock
    private LoyaltyEarnHttpService loyaltyEarnHttpService;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private LoyaltyEarnServiceImpl service;

    private static LoyaltyEarnDto loyaltyEarn(String value, String policyId, String memberId) {
        return LoyaltyEarnDto.builder()
                .quantity(new LoyaltyEarnDto.QuantityDto(new BigDecimal(value)))
                .policyId(policyId)
                .loyaltyProgramMember(new LoyaltyEarnDto.LoyaltyProgramMemberDto(memberId))
                .build();
    }

    @Test
    void saveLoyaltyEarn_mapsAllThreeFields() {
        when(repository.save(any(LoyaltyEarnRecord.class))).thenAnswer(inv -> {
            LoyaltyEarnRecord r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        LoyaltyEarnResponseDto response = service.saveLoyaltyEarn(loyaltyEarn("25.50", "POL-1001", "LPM-2001"));

        ArgumentCaptor<LoyaltyEarnRecord> captor = ArgumentCaptor.forClass(LoyaltyEarnRecord.class);
        verify(repository).save(captor.capture());
        LoyaltyEarnRecord saved = captor.getValue();
        assertThat(saved.getPlanContribution()).isEqualByComparingTo("25.50");
        assertThat(saved.getPolicyId()).isEqualTo("POL-1001");
        assertThat(saved.getLoyaltyProgramMember()).isEqualTo("LPM-2001");
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getPolicyId()).isEqualTo("POL-1001");
    }

    @Test
    void saveLoyaltyEarn_nullNestedObjects_savesNulls() {
        LoyaltyEarnDto request = LoyaltyEarnDto.builder().policyId("POL-1").build();
        when(repository.save(any(LoyaltyEarnRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        LoyaltyEarnResponseDto response = service.saveLoyaltyEarn(request);

        assertThat(response.getPlanContribution()).isNull();
        assertThat(response.getLoyaltyProgramMember()).isNull();
        assertThat(response.getPolicyId()).isEqualTo("POL-1");
    }

    @Test
    void saveLoyaltyEarn_nullRequest_throws() {
        assertThatThrownBy(() -> service.saveLoyaltyEarn(null))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(repository);
    }

    @Test
    @SuppressWarnings("unchecked")
    void syncLoyaltyEarnsFromApi_savesEveryRecordFromApi() {
        when(loyaltyEarnHttpService.getAllLoyaltyEarns()).thenReturn(List.of(
                loyaltyEarn("10", "POL-1", "LPM-1"),
                loyaltyEarn("20.75", "POL-2", "LPM-2")));
        when(repository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<LoyaltyEarnResponseDto> result = service.syncLoyaltyEarnsFromApi();

        ArgumentCaptor<List<LoyaltyEarnRecord>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(captor.capture());
        List<LoyaltyEarnRecord> saved = captor.getValue();
        assertThat(saved).hasSize(2);
        assertThat(saved.get(1).getPlanContribution()).isEqualByComparingTo("20.75");
        assertThat(saved.get(1).getPolicyId()).isEqualTo("POL-2");
        assertThat(saved.get(1).getLoyaltyProgramMember()).isEqualTo("LPM-2");
        assertThat(result).extracting(LoyaltyEarnResponseDto::getPolicyId).containsExactly("POL-1", "POL-2");
    }

    @Test
    void syncLoyaltyEarnsFromApi_emptyApiResponse_savesNothing() {
        when(loyaltyEarnHttpService.getAllLoyaltyEarns()).thenReturn(Collections.emptyList());
        when(repository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.syncLoyaltyEarnsFromApi()).isEmpty();
    }
}
