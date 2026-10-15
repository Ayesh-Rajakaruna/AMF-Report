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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoyaltyEarnServiceImplTest {

    private static final String LOYALTY_EARN_URL = "https://example.test/loyaltyEarn";
    private static final String LOYALTY_RULE_URL = "https://example.test/loyaltyRule";
    private static final String PROGRAM_MEMBER_URL = "https://example.test/loyaltyProgramMember";
    private static final String ACCOUNT_BALANCE_URL = "https://example.test/loyaltyAccountBalance";
    private static final Path REPORT_FILE = Path.of("reports", "LoyaltyEarnRecord_20261015_104900.xlsx");
    private static final AtomicInteger ID_SEQUENCE = new AtomicInteger();

    @Mock
    private HttpService httpService;

    @Mock
    private ExcelService excelService;

    @Mock
    private LoyaltyEarnRecordExcelWriter excelWriter;

    @InjectMocks
    private LoyaltyEarnServiceImpl service;

    @BeforeEach
    void setUp() {
        lenient().when(excelService.openLoyaltyEarnRecordWriter()).thenReturn(excelWriter);
        lenient().when(excelWriter.finish()).thenReturn(REPORT_FILE);
        ReflectionTestUtils.setField(service, "loyaltyEarnUrl", LOYALTY_EARN_URL);
        ReflectionTestUtils.setField(service, "loyaltyRuleUrl", LOYALTY_RULE_URL);
        ReflectionTestUtils.setField(service, "loyaltyProgramMemberUrl", PROGRAM_MEMBER_URL);
        ReflectionTestUtils.setField(service, "loyaltyAccountBalanceUrl", ACCOUNT_BALANCE_URL);
    }

    private void apiReturns(List<LoyaltyEarnDto> loyaltyEarns) {
        stubGet(LOYALTY_EARN_URL, loyaltyEarns);
    }

    @SuppressWarnings("unchecked")
    private void stubGet(String url, Object body) {
        when(httpService.get(eq(url), any(ParameterizedTypeReference.class))).thenReturn(body);
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
                .id("LE-" + ID_SEQUENCE.incrementAndGet())
                .quantity(value == null ? null : new LoyaltyEarnDto.QuantityDto(new BigDecimal(value)))
                .loyaltyProgramMember(memberId == null ? null : new LoyaltyEarnDto.LoyaltyProgramMemberDto(memberId))
                .usageCharacteristic(characteristics)
                .build();
    }

    private static LoyaltyAccountBalanceDto balance(String type, String accountId, String cidn, String value) {
        return LoyaltyAccountBalanceDto.builder()
                .balanceType(type)
                .relatedAccount(new LoyaltyAccountBalanceDto.RelatedAccountDto(accountId, cidn))
                .amount(new LoyaltyAccountBalanceDto.AmountDto(new BigDecimal(value)))
                .build();
    }

    private static ProgramMemberDto member(String id, String name, String characteristicName, String value) {
        return ProgramMemberDto.builder()
                .id(id)
                .name(name)
                .characteristic(List.of(new CharacteristicDto(characteristicName, value)))
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<LoyaltyEarnRecord> captureWritten() {
        ArgumentCaptor<List<LoyaltyEarnRecord>> captor = ArgumentCaptor.forClass(List.class);
        verify(excelWriter).append(captor.capture());
        return captor.getValue();
    }

    @Test
    void sync_mapsIdPlanContributionAndEarningType() {
        LoyaltyEarnDto earn = loyaltyEarn("13.825570", "Monthly", "POL-1", "LPM-1");
        apiReturns(List.of(earn));

        service.syncLoyaltyEarnsFromApi();

        LoyaltyEarnRecord written = captureWritten().get(0);
        assertThat(written.getId()).isEqualTo(earn.getId());
        assertThat(written.getPlanContribution()).isEqualByComparingTo("13.825570");
        assertThat(written.getEarningType()).isEqualTo("Monthly");
    }

    @Test
    void sync_keepsOnlyMonthlyAndUpfront() {
        apiReturns(List.of(
                loyaltyEarn("10", "Monthly", "POL-1", "LPM-1"),
                loyaltyEarn("20", "Upfront", "POL-2", "LPM-2"),
                loyaltyEarn("30", "ManualCredit", "POL-3", "LPM-3"),
                loyaltyEarn("40", null, "POL-4", "LPM-4")));

        service.syncLoyaltyEarnsFromApi();

        assertThat(captureWritten())
                .extracting(LoyaltyEarnRecord::getEarningType)
                .containsExactly("Monthly", "Upfront");
    }

    @Test
    void sync_earningTypeMatchIsCaseInsensitiveAndWrittenInCanonicalCase() {
        apiReturns(List.of(loyaltyEarn("5", " upfront ", "POL-1", "LPM-1")));

        service.syncLoyaltyEarnsFromApi();

        assertThat(captureWritten().get(0).getEarningType()).isEqualTo("Upfront");
    }

    @Test
    void sync_missingOptionalFields_writesNulls() {
        apiReturns(List.of(loyaltyEarn(null, "Upfront", null, null)));

        service.syncLoyaltyEarnsFromApi();

        LoyaltyEarnRecord written = captureWritten().get(0);
        assertThat(written.getPlanContribution()).isNull();
        assertThat(written.getPolicyName()).isNull();
        assertThat(written.getOrganizationName()).isNull();
        assertThat(written.getEarningType()).isEqualTo("Upfront");
    }

    @Test
    void sync_enrichesWithPolicyNameMemberAndTotalBalance() {
        apiReturns(List.of(loyaltyEarn("10", "Monthly", "POL-1", "LPM-1")));
        stubGet(LOYALTY_RULE_URL, List.of(
                LoyaltyRuleDto.builder().id("POL-1").policyName("Gold Policy").build(),
                LoyaltyRuleDto.builder().id("POL-2").policyName("Other").build()));
        stubGet(PROGRAM_MEMBER_URL, List.of(member("LPM-1", "Acme Pty Ltd", "CIDN", "6781234778")));
        stubGet(ACCOUNT_BALANCE_URL, List.of(
                balance("UpfrontBalanceTotal", "ACC-1", "6781234778", "6402.11"),
                balance("EarningBalanceTotal", "ACC-1", "6781234778", "65344.81"),
                balance("ManualCreditBalanceTotal", "ACC-1", "6781234778", "82923.235"),
                balance("SomeOtherBalance", "ACC-1", "6781234778", "999"),
                balance("UpfrontBalanceTotal", "ACC-2", "1111111111", "5")));

        service.syncLoyaltyEarnsFromApi();

        LoyaltyEarnRecord written = captureWritten().get(0);
        assertThat(written.getPolicyName()).isEqualTo("Gold Policy");
        assertThat(written.getOrganizationName()).isEqualTo("Acme Pty Ltd");
        assertThat(written.getCidn()).isEqualTo("6781234778");
        assertThat(written.getTotalBalance()).isEqualByComparingTo("154670.155");
    }

    @Test
    void sync_memberWithoutCidnCharacteristic_usesFirstCharacteristicValue() {
        apiReturns(List.of(loyaltyEarn("10", "Upfront", "POL-1", "LPM-1")));
        stubGet(PROGRAM_MEMBER_URL, List.of(member("LPM-1", "Acme", "AccountNumber", "5550001111")));

        service.syncLoyaltyEarnsFromApi();

        assertThat(captureWritten().get(0).getCidn()).isEqualTo("5550001111");
    }

    @Test
    void sync_totalBalanceIgnoresRelatedAccountId() {
        apiReturns(List.of(loyaltyEarn("10", "Upfront", "POL-1", "LPM-1")));
        stubGet(PROGRAM_MEMBER_URL, List.of(member("LPM-1", "Acme", "CIDN", "6781234778")));
        stubGet(ACCOUNT_BALANCE_URL, List.of(balance("UpfrontBalanceTotal", "LPM-1", "9999999999", "100")));

        service.syncLoyaltyEarnsFromApi();

        assertThat(captureWritten().get(0).getTotalBalance()).isNull();
    }

    @Test
    void sync_noLookupData_leavesEnrichedColumnsNull() {
        apiReturns(List.of(loyaltyEarn("10", "Monthly", "POL-1", "LPM-1")));

        service.syncLoyaltyEarnsFromApi();

        LoyaltyEarnRecord written = captureWritten().get(0);
        assertThat(written.getPolicyName()).isNull();
        assertThat(written.getOrganizationName()).isNull();
        assertThat(written.getCidn()).isNull();
        assertThat(written.getTotalBalance()).isNull();
    }

    @Test
    void sync_returnsReportFileAndRecordCount() {
        apiReturns(List.of(loyaltyEarn("10", "Monthly", "POL-1", "LPM-1")));
        when(excelWriter.getRecordCount()).thenReturn(1);

        LoyaltyEarnReportResponseDto response = service.syncLoyaltyEarnsFromApi();

        assertThat(response.fileName()).isEqualTo("LoyaltyEarnRecord_20261015_104900.xlsx");
        assertThat(response.filePath()).isEqualTo(REPORT_FILE.toAbsolutePath().toString());
        assertThat(response.recordCount()).isEqualTo(1);

        InOrder inOrder = inOrder(excelWriter);
        inOrder.verify(excelWriter).append(anyList());
        inOrder.verify(excelWriter).finish();
        inOrder.verify(excelWriter).close();
    }

    @Test
    @SuppressWarnings("unchecked")
    void sync_writesExcelInBatchesOf1000() {
        List<LoyaltyEarnDto> earns = new ArrayList<>();
        for (int i = 0; i < 2500; i++) {
            earns.add(loyaltyEarn("1", "Monthly", "POL-1", "LPM-1"));
        }
        apiReturns(earns);

        service.syncLoyaltyEarnsFromApi();

        ArgumentCaptor<List<LoyaltyEarnRecord>> captor = ArgumentCaptor.forClass(List.class);
        verify(excelWriter, times(3)).append(captor.capture());
        assertThat(captor.getAllValues()).extracting(List::size).containsExactly(1000, 1000, 500);
        verify(excelWriter).finish();
    }

    @Test
    void sync_recordWithoutCharacteristics_isSkipped() {
        apiReturns(List.of(LoyaltyEarnDto.builder().id("LE-X").build()));

        service.syncLoyaltyEarnsFromApi();

        verify(excelWriter, never()).append(anyList());
        verify(excelWriter).finish();
    }

    @Test
    void sync_emptyApiResponse_writesHeaderOnlyFile() {
        apiReturns(Collections.emptyList());

        service.syncLoyaltyEarnsFromApi();

        verify(excelWriter, never()).append(anyList());
        verify(excelWriter).finish();
        verify(excelWriter).close();
    }

    @Test
    void sync_nullApiBody_writesHeaderOnlyFile() {
        apiReturns(null);

        service.syncLoyaltyEarnsFromApi();

        verify(excelWriter, never()).append(anyList());
        verify(excelWriter).finish();
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
}
