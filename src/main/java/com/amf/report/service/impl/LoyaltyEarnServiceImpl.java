package com.amf.report.service.impl;

import com.amf.report.dto.LoyaltyEarnDto;
import com.amf.report.dto.LoyaltyEarnResponseDto;
import com.amf.report.entity.LoyaltyEarnRecord;
import com.amf.report.repository.LoyaltyEarnRecordRepository;
import com.amf.report.service.HttpService;
import com.amf.report.service.LoyaltyEarnService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.amf.report.constant.AppConstants.ALLOWED_EARNING_TYPES;
import static com.amf.report.constant.AppConstants.CHARACTERISTIC_EARNING_TYPE;
import static com.amf.report.constant.AppConstants.CHARACTERISTIC_POLICY_ID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoyaltyEarnServiceImpl implements LoyaltyEarnService {

    private static final ParameterizedTypeReference<List<LoyaltyEarnDto>> LOYALTY_EARN_LIST =
            new ParameterizedTypeReference<>() {};

    private final LoyaltyEarnRecordRepository loyaltyEarnRecordRepository;
    private final HttpService httpService;
    private final ModelMapper modelMapper;

    @Value("${loyalty.api.loyalty-earn-url}")
    private String loyaltyEarnUrl;

    @Override
    @Transactional
    public List<LoyaltyEarnResponseDto> syncLoyaltyEarnsFromApi() {
        List<LoyaltyEarnDto> loyaltyEarns = Optional
                .ofNullable(httpService.get(loyaltyEarnUrl, LOYALTY_EARN_LIST))
                .orElse(Collections.emptyList());

        List<LoyaltyEarnRecord> records = loyaltyEarns.stream()
                .filter(Objects::nonNull)
                .filter(this::isMonthlyOrUpfront)
                .map(this::toEntity)
                .toList();
        log.info("loyaltyEarn API returned {} record(s); {} are Monthly/Upfront",
                loyaltyEarns.size(), records.size());

        List<LoyaltyEarnRecord> saved = loyaltyEarnRecordRepository.saveAll(records);
        log.info("Saved {} loyaltyEarn record(s) to MySQL", saved.size());
        return saved.stream().map(this::toResponse).toList();
    }

    private boolean isMonthlyOrUpfront(LoyaltyEarnDto loyaltyEarn) {
        return normalizeEarningType(loyaltyEarn.getUsageCharacteristicValue(CHARACTERISTIC_EARNING_TYPE)) != null;
    }

    /** Returns "Monthly" or "Upfront" (canonical case) for an allowed type, otherwise null. */
    private String normalizeEarningType(String earningType) {
        if (earningType == null) {
            return null;
        }
        return ALLOWED_EARNING_TYPES.stream()
                .filter(allowed -> allowed.equalsIgnoreCase(earningType.trim()))
                .findFirst()
                .orElse(null);
    }

    private LoyaltyEarnRecord toEntity(LoyaltyEarnDto loyaltyEarn) {
        return LoyaltyEarnRecord.builder()
                .planContribution(extractPlanContribution(loyaltyEarn))
                .policyId(loyaltyEarn.getUsageCharacteristicValue(CHARACTERISTIC_POLICY_ID))
                .loyaltyProgramMember(extractLoyaltyProgramMemberId(loyaltyEarn))
                .earningType(normalizeEarningType(loyaltyEarn.getUsageCharacteristicValue(CHARACTERISTIC_EARNING_TYPE)))
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
