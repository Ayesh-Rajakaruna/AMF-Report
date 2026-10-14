package com.amf.report.service.impl;

import com.amf.report.dto.LoyaltyEarnRequestDto;
import com.amf.report.dto.LoyaltyEarnResponseDto;
import com.amf.report.entity.LoyaltyEarnRecord;
import com.amf.report.repository.LoyaltyEarnRecordRepository;
import com.amf.report.service.LoyaltyEarnService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoyaltyEarnServiceImpl implements LoyaltyEarnService {

    private final LoyaltyEarnRecordRepository loyaltyEarnRecordRepository;

    @Override
    @Transactional
    public LoyaltyEarnResponseDto saveLoyaltyEarn(LoyaltyEarnRequestDto loyaltyEarn) {
        if (loyaltyEarn == null) {
            throw new IllegalArgumentException("loyaltyEarn must not be null");
        }

        LoyaltyEarnRecord record = LoyaltyEarnRecord.builder()
                .planContribution(extractPlanContribution(loyaltyEarn))
                .policyId(loyaltyEarn.getPolicyId())
                .loyaltyProgramMember(extractLoyaltyProgramMemberId(loyaltyEarn))
                .build();

        return toResponse(loyaltyEarnRecordRepository.save(record));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoyaltyEarnResponseDto> getAllLoyaltyEarns() {
        return loyaltyEarnRecordRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private BigDecimal extractPlanContribution(LoyaltyEarnRequestDto loyaltyEarn) {
        return Optional.ofNullable(loyaltyEarn.getQuantity())
                .map(LoyaltyEarnRequestDto.QuantityDto::getValue)
                .orElse(null);
    }

    private String extractLoyaltyProgramMemberId(LoyaltyEarnRequestDto loyaltyEarn) {
        return Optional.ofNullable(loyaltyEarn.getLoyaltyProgramMember())
                .map(LoyaltyEarnRequestDto.LoyaltyProgramMemberDto::getId)
                .orElse(null);
    }

    private LoyaltyEarnResponseDto toResponse(LoyaltyEarnRecord record) {
        return LoyaltyEarnResponseDto.builder()
                .id(record.getId())
                .planContribution(record.getPlanContribution())
                .policyId(record.getPolicyId())
                .loyaltyProgramMember(record.getLoyaltyProgramMember())
                .createdAt(record.getCreatedAt())
                .build();
    }
}
