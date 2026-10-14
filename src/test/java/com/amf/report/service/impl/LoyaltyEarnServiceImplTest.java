package com.amf.report.service.impl;

import com.amf.report.dto.LoyaltyEarnRequestDto;
import com.amf.report.dto.LoyaltyEarnResponseDto;
import com.amf.report.entity.LoyaltyEarnRecord;
import com.amf.report.repository.LoyaltyEarnRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoyaltyEarnServiceImplTest {

    @Mock
    private LoyaltyEarnRecordRepository repository;

    @InjectMocks
    private LoyaltyEarnServiceImpl service;

    @Test
    void saveLoyaltyEarn_mapsAllThreeFields() {
        LoyaltyEarnRequestDto request = LoyaltyEarnRequestDto.builder()
                .quantity(new LoyaltyEarnRequestDto.QuantityDto(new BigDecimal("25.50")))
                .policyId("POL-1001")
                .loyaltyProgramMember(new LoyaltyEarnRequestDto.LoyaltyProgramMemberDto("LPM-2001"))
                .build();
        when(repository.save(any(LoyaltyEarnRecord.class))).thenAnswer(inv -> {
            LoyaltyEarnRecord r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        LoyaltyEarnResponseDto response = service.saveLoyaltyEarn(request);

        ArgumentCaptor<LoyaltyEarnRecord> captor = ArgumentCaptor.forClass(LoyaltyEarnRecord.class);
        verify(repository).save(captor.capture());
        LoyaltyEarnRecord saved = captor.getValue();
        assertThat(saved.getPlanContribution()).isEqualByComparingTo("25.50");
        assertThat(saved.getPolicyId()).isEqualTo("POL-1001");
        assertThat(saved.getLoyaltyProgramMember()).isEqualTo("LPM-2001");
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void saveLoyaltyEarn_nullNestedObjects_savesNulls() {
        LoyaltyEarnRequestDto request = LoyaltyEarnRequestDto.builder().policyId("POL-1").build();
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
}
