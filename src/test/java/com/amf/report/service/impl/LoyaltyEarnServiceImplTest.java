package com.amf.report.service.impl;

import com.amf.report.dto.LoyaltyEarnDto;
import com.amf.report.dto.CharacteristicDto;
import com.amf.report.dto.LoyaltyAccountBalanceDto;
import com.amf.report.dto.LoyaltyEarnData;
import com.amf.report.dto.LoyaltyRuleDto;
import com.amf.report.dto.ProgramMemberDto;
import com.amf.report.dto.LoyaltyEarnResponseDto;
import com.amf.report.entity.LoyaltyEarnRecord;
import com.amf.report.repository.LoyaltyEarnRecordRepository;
import com.amf.report.service.ExcelService;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoyaltyEarnServiceImplTest {
    private static final String LOYALTY_EARN_URL = "https://example.test/loyaltyEarn";
    private static final String LOYALTY_RULE_URL = "https://example.test/loyaltyRule";
    private static final String PROGRAM_MEMBER_URL = "https://example.test/loyaltyProgramMember";
    private static final String ACCOUNT_BALANCE_URL = "https://example.test/loyaltyAccountBalance";

    @Mock
    private LoyaltyEarnRecordRepository repository;

    @Mock
    private HttpService httpService;

    @Mock
    private ExcelService excelService;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private LoyaltyEarnServiceImpl service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "loyaltyEarnUrl", LOYALTY_EARN_URL);
        ReflectionTestUtils.setField(service, "loyaltyRuleUrl", LOYALTY_RULE_URL);
        ReflectionTestUtils.setField(service, "loyaltyProgramMemberUrl", PROGRAM_MEMBER_URL);
        ReflectionTestUtils.setField(service, "loyaltyAccountBalanceUrl", ACCOUNT_BALANCE_URL);
    }

    private void apiReturns(List<LoyaltyEarnDto> loyaltyEarns) {
        stubGet(LOYALTY_EARN_URL, loyaltyEarns);
    }

    private void stubGet(String url, Object body) {
        when(httpService.get(eq(url), any(ParameterizedTypeReference.class))).thenReturn(body);
    }

    private static LoyaltyAccountBalanceDto balance(String type, String accountId, String accountName, String value) {
        return LoyaltyAccountBalanceDto.builder()
                .balanceType(type)
                .relatedAccount(new LoyaltyAccountBalanceDto.RelatedAccountDto(accountId, accountName))
                .amount(new LoyaltyAccountBalanceDto.AmountDto(new BigDecimal(value)))
                .build();
    }

    private void saveAllReturnsInput() {
        when(repository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
    }

    private static final AtomicInteger ID_SEQUENCE = new AtomicInteger();

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
                .id("LE-" + ID_SEQUENCE.incrementAndGet())
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
    void sync_mapsPlanContributionAndEarningType() {
        LoyaltyEarnDto earn = loyaltyEarn("13.825570", "Monthly", "POL-1", "LPM-1");
        apiReturns(List.of(earn));
        saveAllReturnsInput();

        List<LoyaltyEarnResponseDto> result = service.syncLoyaltyEarnsFromApi();

        LoyaltyEarnRecord saved = captureSaved().get(0);
        assertThat(saved.getId()).isEqualTo(earn.getId());
        assertThat(result.get(0).getId()).isEqualTo(earn.getId());
        assertThat(saved.getPlanContribution()).isEqualByComparingTo("13.825570");
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
        assertThat(result).extracting(LoyaltyEarnResponseDto::getPlanContribution)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("10"), new BigDecimal("20"));
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
        assertThat(result.getPolicyName()).isNull();
        assertThat(result.getOrganizationName()).isNull();
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

    @Test
    void sync_enrichesWithPolicyNameMemberAndTotalBalance() {
        apiReturns(List.of(loyaltyEarn("10", "Monthly", "POL-1", "LPM-1")));
        stubGet(LOYALTY_RULE_URL, List.of(
                LoyaltyRuleDto.builder().id("POL-1").policyName("Gold Policy").build(),
                LoyaltyRuleDto.builder().id("POL-2").policyName("Other").build()));
        stubGet(PROGRAM_MEMBER_URL, List.of(ProgramMemberDto.builder()
                .id("LPM-1").name("Acme Pty Ltd")
                .characteristic(List.of(new CharacteristicDto("CIDN", "6781234778")))
                .build()));
        stubGet(ACCOUNT_BALANCE_URL, List.of(
                balance("UpfrontBalanceTotal", "LPM-1", "6781234778", "6402.11"),
                balance("EarningBalanceTotal", "LPM-1", "6781234778", "65344.81"),
                balance("ManualCreditBalanceTotal", "LPM-1", "6781234778", "82923.235"),
                balance("SomeOtherBalance", "LPM-1", "6781234778", "999"),
                balance("UpfrontBalanceTotal", "LPM-OTHER", "1111111111", "5")));
        saveAllReturnsInput();

        service.syncLoyaltyEarnsFromApi();

        LoyaltyEarnRecord saved = captureSaved().get(0);
        assertThat(saved.getPolicyName()).isEqualTo("Gold Policy");
        assertThat(saved.getOrganizationName()).isEqualTo("Acme Pty Ltd");
        assertThat(saved.getCidn()).isEqualTo("6781234778");
        assertThat(saved.getTotalBalance()).isEqualByComparingTo("154670.155");
    }

    @Test
    void sync_memberWithoutCidnCharacteristic_usesFirstCharacteristicValue() {
        apiReturns(List.of(loyaltyEarn("10", "Upfront", "POL-1", "LPM-1")));
        stubGet(PROGRAM_MEMBER_URL, List.of(ProgramMemberDto.builder()
                .id("LPM-1").name("Acme")
                .characteristic(List.of(new CharacteristicDto("AccountNumber", "5550001111")))
                .build()));
        saveAllReturnsInput();

        service.syncLoyaltyEarnsFromApi();

        assertThat(captureSaved().get(0).getCidn()).isEqualTo("5550001111");
    }

    @Test
    void sync_totalBalanceSummedPerCidn() {
        apiReturns(List.of(loyaltyEarn("10", "Upfront", "POL-1", "LPM-1")));
        stubGet(PROGRAM_MEMBER_URL, List.of(ProgramMemberDto.builder()
                .id("LPM-1").name("Acme")
                .characteristic(List.of(new CharacteristicDto("CIDN", "6781234778")))
                .build()));
        stubGet(ACCOUNT_BALANCE_URL, List.of(
                balance("UpfrontBalanceTotal", "ACC-9", "6781234778", "100"),
                balance("EarningBalanceTotal", "ACC-9", "6781234778", "50.5")));
        saveAllReturnsInput();

        service.syncLoyaltyEarnsFromApi();

        assertThat(captureSaved().get(0).getTotalBalance()).isEqualByComparingTo("150.5");
    }

    @Test
    void sync_noLookupData_leavesEnrichedColumnsNull() {
        apiReturns(List.of(loyaltyEarn("10", "Monthly", "POL-1", "LPM-1")));
        saveAllReturnsInput();

        service.syncLoyaltyEarnsFromApi();

        LoyaltyEarnRecord saved = captureSaved().get(0);
        assertThat(saved.getPolicyName()).isNull();
        assertThat(saved.getOrganizationName()).isNull();
        assertThat(saved.getCidn()).isNull();
        assertThat(saved.getTotalBalance()).isNull();
    }

    @Test
    void sync_totalBalanceIgnoresRelatedAccountId() {
        apiReturns(List.of(loyaltyEarn("10", "Upfront", "POL-1", "LPM-1")));
        stubGet(PROGRAM_MEMBER_URL, List.of(ProgramMemberDto.builder()
                .id("LPM-1").name("Acme")
                .characteristic(List.of(new CharacteristicDto("CIDN", "6781234778")))
                .build()));

        stubGet(ACCOUNT_BALANCE_URL, List.of(
                balance("UpfrontBalanceTotal", "LPM-1", "9999999999", "100")));
        saveAllReturnsInput();

        service.syncLoyaltyEarnsFromApi();

        assertThat(captureSaved().get(0).getTotalBalance()).isNull();
    }

    @Test
    void toMonthlyOrUpfrontEarns_keepsOnlyNeededFields() {
        LoyaltyEarnDto monthly = loyaltyEarn("13.5", "Monthly", "POL-1", "LPM-1");
        LoyaltyEarnDto withoutId = loyaltyEarn("9", "Upfront", "POL-3", "LPM-3");
        withoutId.setId(null);

        List<LoyaltyEarnData> result = service.toMonthlyOrUpfrontEarns(Arrays.asList(
                monthly,
                loyaltyEarn("7", "ManualCredit", "POL-2", "LPM-2"),
                withoutId,
                null));

        assertThat(result).containsExactly(
                new LoyaltyEarnData(monthly.getId(), new BigDecimal("13.5"), "POL-1", "LPM-1", "Monthly"));
    }

    @Test
    void sync_writesSavedRecordsToExcel() {
        apiReturns(List.of(loyaltyEarn("10", "Monthly", "POL-1", "LPM-1")));
        saveAllReturnsInput();

        service.syncLoyaltyEarnsFromApi();

        List<LoyaltyEarnRecord> saved = captureSaved();
        verify(excelService).writeLoyaltyEarnRecords(saved);
    }
}
