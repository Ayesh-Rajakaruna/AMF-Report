package com.amf.report.service.impl;

import com.amf.report.dto.CharacteristicDto;
import com.amf.report.dto.LoyaltyAccountBalanceDto;
import com.amf.report.dto.LoyaltyEarnData;
import com.amf.report.dto.LoyaltyEarnDto;
import com.amf.report.dto.LoyaltyEarnRecord;
import com.amf.report.dto.LoyaltyEarnReportResponseDto;
import com.amf.report.dto.LoyaltyRuleDto;
import com.amf.report.dto.ProgramMemberDto;
import com.amf.report.excel.LoyaltyEarnRecordExcelWriter;
import com.amf.report.service.ExcelService;
import com.amf.report.service.HttpService;
import com.amf.report.service.LoyaltyEarnService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.amf.report.constant.AppConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoyaltyEarnServiceImpl implements LoyaltyEarnService {
    private final HttpService httpService;
    private final ExcelService excelService;

    @Value("${loyalty.api.loyalty-earn-url}")
    private String loyaltyEarnUrl;

    @Value("${loyalty.api.loyalty-rule-url}")
    private String loyaltyRuleUrl;

    @Value("${loyalty.api.loyalty-program-member-url}")
    private String loyaltyProgramMemberUrl;

    @Value("${loyalty.api.loyalty-account-balance-url}")
    private String loyaltyAccountBalanceUrl;

    @Override
    public LoyaltyEarnReportResponseDto syncLoyaltyEarnsFromApi() {
        List<LoyaltyEarnData> loyaltyEarns = loadMonthlyOrUpfrontEarns();

        Map<String, String> policyNameById = loadPolicyNames();
        Map<String, Pair<String, String>> memberById = loadMembers();
        Map<String, BigDecimal> totalBalanceByCidn = loadTotalBalances();

        try (LoyaltyEarnRecordExcelWriter excelWriter = excelService.openLoyaltyEarnRecordWriter()) {
            for (int from = 0; from < loyaltyEarns.size(); from += SYNC_BATCH_SIZE) {
                int to = Math.min(from + SYNC_BATCH_SIZE, loyaltyEarns.size());
                List<LoyaltyEarnRecord> batch = loyaltyEarns.subList(from, to).stream()
                        .map(earn -> toRecord(earn, policyNameById, memberById, totalBalanceByCidn))
                        .toList();
                excelWriter.append(batch);
                log.info("Wrote loyaltyEarn records {}-{} of {} to Excel", from + 1, to, loyaltyEarns.size());
            }
            Path file = excelWriter.finish();
            return new LoyaltyEarnReportResponseDto(
                    file.getFileName().toString(), file.toAbsolutePath().toString(), excelWriter.getRecordCount());
        }
    }

    List<LoyaltyEarnData> loadMonthlyOrUpfrontEarns() {
        List<LoyaltyEarnDto> response = fetchList(loyaltyEarnUrl, LOYALTY_EARN_LIST);
        List<LoyaltyEarnData> loyaltyEarns = toMonthlyOrUpfrontEarns(response);
        log.info("loyaltyEarn API returned {} record(s); {} are Monthly/Upfront",
                response.size(), loyaltyEarns.size());
        return loyaltyEarns;
    }

    List<LoyaltyEarnData> toMonthlyOrUpfrontEarns(List<LoyaltyEarnDto> loyaltyEarns) {
        return loyaltyEarns.stream()
                .filter(Objects::nonNull)
                .map(this::toLoyaltyEarnData)
                .filter(Objects::nonNull)
                .toList();
    }

    private LoyaltyEarnData toLoyaltyEarnData(LoyaltyEarnDto loyaltyEarn) {
        if (!StringUtils.hasText(loyaltyEarn.getId())) {
            return null;
        }
        String earningType = normalizeEarningType(loyaltyEarn.getUsageCharacteristicValue(CHARACTERISTIC_EARNING_TYPE));
        if (earningType == null) {
            return null;
        }
        return new LoyaltyEarnData(
                loyaltyEarn.getId(),
                extractPlanContribution(loyaltyEarn),
                loyaltyEarn.getUsageCharacteristicValue(CHARACTERISTIC_POLICY_ID),
                extractLoyaltyProgramMemberId(loyaltyEarn),
                earningType);
    }

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

    Map<String, Pair<String, String>> loadMembers() {
        Map<String, Pair<String, String>> memberById = new HashMap<>();
        for (ProgramMemberDto member : fetchList(loyaltyProgramMemberUrl, PROGRAM_MEMBER_LIST)) {
            if (member != null && member.getId() != null) {
                memberById.putIfAbsent(member.getId(), Pair.of(member.getName(), extractCidn(member)));
            }
        }
        log.info("Loaded {} program member(s)", memberById.size());
        return memberById;
    }

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

    private static String extractCidn(ProgramMemberDto member) {
        String cidn = CharacteristicDto.findValue(member.getCharacteristic(), CHARACTERISTIC_CIDN);
        return cidn != null ? cidn : CharacteristicDto.firstValue(member.getCharacteristic());
    }

    private <T> List<T> fetchList(String url, ParameterizedTypeReference<List<T>> type) {
        return Optional.ofNullable(httpService.get(url, type)).orElse(Collections.emptyList());
    }

    private String normalizeEarningType(String earningType) {
        if (earningType == null) {
            return null;
        }
        return ALLOWED_EARNING_TYPES.stream()
                .filter(allowed -> allowed.equalsIgnoreCase(earningType.trim()))
                .findFirst()
                .orElse(null);
    }

    private LoyaltyEarnRecord toRecord(LoyaltyEarnData loyaltyEarn,
                                       Map<String, String> policyNameById,
                                       Map<String, Pair<String, String>> memberById,
                                       Map<String, BigDecimal> totalBalanceByCidn) {
        String policyId = loyaltyEarn.policyId();
        String memberId = loyaltyEarn.memberId();
        Pair<String, String> member = memberId == null ? null : memberById.get(memberId);
        String cidn = member == null ? null : member.getRight();
        BigDecimal totalBalance = cidn == null ? null : totalBalanceByCidn.get(cidn.trim());

        return LoyaltyEarnRecord.builder()
                .planContribution(loyaltyEarn.planContribution())
                .earningType(loyaltyEarn.earningType())
                .policyName(policyId == null ? null : policyNameById.get(policyId))
                .organizationName(member == null ? null : member.getLeft())
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
}
