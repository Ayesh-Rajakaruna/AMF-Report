package com.amf.report.loyalty;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoyaltyEarnServiceImpl implements LoyaltyEarnService {

    private final LoyaltyEarnClient loyaltyEarnClient;
    private final LoyaltyEarnRecordRepository loyaltyEarnRecordRepository;

    @Override
    @Transactional
    public LoyaltyEarnRecord processLoyaltyEarn(LoyaltyEarnRequest request) {
        LoyaltyEarn loyaltyEarn = loyaltyEarnClient.getLoyaltyEarn(request);
        if (loyaltyEarn == null) {
            return null;
        }

        LoyaltyEarnRecord record = new LoyaltyEarnRecord();
        record.setPlanContribution(extractPlanContribution(loyaltyEarn));
        record.setPolicyId(loyaltyEarn.getPolicyId());
        record.setLoyaltyProgramMember(extractLoyaltyProgramMemberId(loyaltyEarn));

        return loyaltyEarnRecordRepository.save(record);
    }

    private BigDecimal extractPlanContribution(LoyaltyEarn loyaltyEarn) {
        return Optional.ofNullable(loyaltyEarn.getQuantity())
                .map(Quantity::getValue)
                .map(BigDecimal::new) // drop if value is already BigDecimal
                .orElse(null);
    }

    private String extractLoyaltyProgramMemberId(LoyaltyEarn loyaltyEarn) {
        return Optional.ofNullable(loyaltyEarn.getLoyaltyProgramMember())
                .map(LoyaltyProgramMember::getId)
                .orElse(null);
    }
}
