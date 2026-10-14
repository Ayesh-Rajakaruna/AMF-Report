package com.amf.report.service.impl;

import com.amf.report.dto.LoyaltyEarnDto;
import com.amf.report.dto.LoyaltyEarnResponseDto;
import com.amf.report.entity.LoyaltyEarnRecord;
import com.amf.report.repository.LoyaltyEarnRecordRepository;
import com.amf.report.service.LoyaltyEarnHttpService;
import com.amf.report.service.LoyaltyEarnService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoyaltyEarnServiceImpl implements LoyaltyEarnService {

    private final LoyaltyEarnRecordRepository loyaltyEarnRecordRepository;
    private final LoyaltyEarnHttpService loyaltyEarnHttpService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public List<LoyaltyEarnResponseDto> syncLoyaltyEarnsFromApi() {
        List<LoyaltyEarnRecord> records = loyaltyEarnHttpService.getAllLoyaltyEarns().stream()
                .filter(Objects::nonNull)
                .map(this::toEntity)
                .toList();

        List<LoyaltyEarnRecord> saved = loyaltyEarnRecordRepository.saveAll(records);
        log.info("Saved {} loyaltyEarn record(s) to MySQL", saved.size());
        return saved.stream().map(this::toResponse).toList();
    }

    private LoyaltyEarnRecord toEntity(LoyaltyEarnDto loyaltyEarn) {
        return LoyaltyEarnRecord.builder()
                .planContribution(extractPlanContribution(loyaltyEarn))
                .policyId(loyaltyEarn.getPolicyId())
                .loyaltyProgramMember(extractLoyaltyProgramMemberId(loyaltyEarn))
                .build();
    }

    private BigDecimal extractPlanContribution(LoyaltyEarnDto loyaltyEarn) {
        return Optional.ofNullable(loyaltyEarn.getQuantity())
                .map(LoyaltyEarnDto.QuantityDto::getValue)
                .orElse(null);
    }

    private String extractLoyaltyProgramMemberId(LoyaltyEarnDto loyaltyEarn) {
        return Optional.ofNullable(loyaltyEarn.getLoyaltyProgramMember())
                .map(LoyaltyEarnDto.LoyaltyProgramMemberDto::getId)
                .orElse(null);
    }

    private LoyaltyEarnResponseDto toResponse(LoyaltyEarnRecord record) {
        return modelMapper.map(record, LoyaltyEarnResponseDto.class);
    }
}
