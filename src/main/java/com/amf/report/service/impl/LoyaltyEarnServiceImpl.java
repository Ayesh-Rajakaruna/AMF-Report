package com.amf.report.service.impl;

import com.amf.report.dto.CharacteristicDto;
import com.amf.report.dto.LoyaltyAccountBalanceDto;
import com.amf.report.dto.LoyaltyEarnDto;
import com.amf.report.dto.LoyaltyEarnResponseDto;
import com.amf.report.dto.LoyaltyRuleDto;
import com.amf.report.dto.MemberInfo;
import com.amf.report.dto.ProgramMemberDto;
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
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.amf.report.constant.AppConstants.ACCOUNT_BALANCE_LIST;
import static com.amf.report.constant.AppConstants.ALLOWED_EARNING_TYPES;
import static com.amf.report.constant.AppConstants.CHARACTERISTIC_CIDN;
import static com.amf.report.constant.AppConstants.CHARACTERISTIC_EARNING_TYPE;
import static com.amf.report.constant.AppConstants.CHARACTERISTIC_POLICY_ID;
import static com.amf.report.constant.AppConstants.LOYALTY_EARN_LIST;
import static com.amf.report.constant.AppConstants.LOYALTY_RULE_LIST;
import static com.amf.report.constant.AppConstants.PROGRAM_MEMBER_LIST;
import static com.amf.report.constant.AppConstants.TOTAL_BALANCE_TYPES;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoyaltyEarnServiceImpl implements LoyaltyEarnService {

    private final LoyaltyEarnRecordRepository loyaltyEarnRecordRepository;
    private final HttpService httpService;
    private final ModelMapper modelMapper;

    @Value("${loyalty.api.loyalty-earn-url}")
    private String loyaltyEarnUrl;

    @Value("${loyalty.api.loyalty-rule-url}")
    private String loyaltyRuleUrl;

    @Value("${loyalty.api.loyalty-program-member-url}")
    private String loyaltyProgramMemberUrl;

    @Value("${loyalty.api.loyalty-account-balance-url}")
    private String loyaltyAccountBalanceUrl;

    @Override
    @Transactional
    public List<LoyaltyEarnResponseDto> syncLoyaltyEarnsFromApi() {
        List<LoyaltyEarnDto> loyaltyEarns = fetchList(loyaltyEarnUrl, LOYALTY_EARN_LIST);
        List<LoyaltyEarnDto> monthlyOrUpfront = loyaltyEarns.stream()
                .filter(Objects::nonNull)
                .filter(this::isMonthlyOrUpfront)
                .toList();
        log.info("loyaltyEarn API returned {} record(s); {} are Monthly/Upfront",
                loyaltyEarns.size(), monthlyOrUpfront.size());

        Map<String, String> policyNameById = loadPolicyNames();
        Map<String, MemberInfo> memberById = loadMembers();
        Map<String, BigDecimal> totalBalanceByCidn = loadTotalBalances();

        List<LoyaltyEarnRecord> records = monthlyOrUpfront.stream()
                .map(earn -> toEntity(earn, policyNameById, memberById, totalBalanceByCidn))
                .toList();

        List<LoyaltyEarnRecord> saved = loyaltyEarnRecordRepository.saveAll(records);
        log.info("Saved {} loyaltyEarn record(s) to MySQL", saved.size());
        return saved.stream().map(this::toResponse).toList();
    }

    // ---------------------------------------------------------------------------------
    // Lookup maps
    // ---------------------------------------------------------------------------------

    /** loyaltyRule.id (= loyaltyEarn PolicyId) -> loyaltyRule.policyName */
    Map<String, String> loadPolicyNames() {
        Map<String, String> policyNameById = new HashMap<>();
        for (LoyaltyRuleDto rule : fetchList(loyaltyRuleUrl, LOYALTY_RULE_LIST)) {
            if (rule != null && rule.getId() != null) {
                policyNameById.putIfAbsent(rule.getId(), rule.getPolicyName());
            }
        }
        log.info("Loaded {} policy name(s)", policyNameById.size());
        return policyNameById;
    }

    /** loyaltyProgramMember.id -> (name, characteristic value = CIDN) */
    Map<String, MemberInfo> loadMembers() {
        Map<String, MemberInfo> memberById = new HashMap<>();
        for (ProgramMemberDto member : fetchList(loyaltyProgramMemberUrl, PROGRAM_MEMBER_LIST)) {
            if (member != null && member.getId() != null) {
                memberById.putIfAbsent(member.getId(), new MemberInfo(member.getName(), extractCidn(member)));
            }
        }
        log.info("Loaded {} program member(s)", memberById.size());
        return memberById;
    }

    /**
     * CIDN (loyaltyAccountBalance relatedAccount.name) -> total balance, where total balance is
     * UpfrontBalanceTotal + EarningBalanceTotal + ManualCreditBalanceTotal for that CIDN.
     */
    Map<String, BigDecimal> loadTotalBalances() {
        Map<String, BigDecimal> totalBalanceByCidn = new HashMap<>();
        for (LoyaltyAccountBalanceDto balance : fetchList(loyaltyAccountBalanceUrl, ACCOUNT_BALANCE_LIST)) {
            if (balance == null || balance.getRelatedAccount() == null || balance.getAmount() == null
                    || balance.getAmount().getValue() == null
                    || !TOTAL_BALANCE_TYPES.contains(balance.getBalanceType())) {
                continue;
            }
            String cidn = balance.getRelatedAccount().getName();
            if (StringUtils.hasText(cidn)) {
                totalBalanceByCidn.merge(cidn.trim(), balance.getAmount().getValue(), BigDecimal::add);
            }
        }
        log.info("Loaded total balance for {} CIDN(s)", totalBalanceByCidn.size());
        return totalBalanceByCidn;
    }

    /** CIDN from the member's "CIDN" characteristic, falling back to the first characteristic value. */
    private static String extractCidn(ProgramMemberDto member) {
        String cidn = CharacteristicDto.findValue(member.getCharacteristic(), CHARACTERISTIC_CIDN);
        return cidn != null ? cidn : CharacteristicDto.firstValue(member.getCharacteristic());
    }

    private <T> List<T> fetchList(String url, ParameterizedTypeReference<List<T>> type) {
        return Optional.ofNullable(httpService.get(url, type)).orElse(Collections.emptyList());
    }

    // ---------------------------------------------------------------------------------
    // Mapping
    // ---------------------------------------------------------------------------------

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

    private LoyaltyEarnRecord toEntity(LoyaltyEarnDto loyaltyEarn,
                                      Map<String, String> policyNameById,
                                      Map<String, MemberInfo> memberById,
                                      Map<String, BigDecimal> totalBalanceByCidn) {
        String policyId = loyaltyEarn.getUsageCharacteristicValue(CHARACTERISTIC_POLICY_ID);
        String memberId = extractLoyaltyProgramMemberId(loyaltyEarn);
        MemberInfo member = memberId == null ? null : memberById.get(memberId);
        String cidn = member == null ? null : member.cidn();

        BigDecimal totalBalance = cidn == null ? null : totalBalanceByCidn.get(cidn.trim());

        return LoyaltyEarnRecord.builder()
                .planContribution(extractPlanContribution(loyaltyEarn))
                .policyId(policyId)
                .loyaltyProgramMember(memberId)
                .earningType(normalizeEarningType(loyaltyEarn.getUsageCharacteristicValue(CHARACTERISTIC_EARNING_TYPE)))
                .policyName(policyId == null ? null : policyNameById.get(policyId))
                .organizationName(member == null ? null : member.organizationName())
                .cidn(cidn)
                .totalBalance(totalBalance)
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
