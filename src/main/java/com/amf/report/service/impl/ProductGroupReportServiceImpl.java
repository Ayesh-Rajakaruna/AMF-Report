package com.amf.report.service.impl;

import com.amf.report.config.ProductGroupApiProperties;
import com.amf.report.constant.ReportEnvironment;
import com.amf.report.dto.OrganizationDto;
import com.amf.report.dto.OrganizationReportFile;
import com.amf.report.dto.OrganizationReportRecord;
import com.amf.report.dto.ProductGroupDto;
import com.amf.report.dto.ProductGroupReportResponseDto;
import com.amf.report.exception.InvalidRequestException;
import com.amf.report.exception.ResourceNotFoundException;
import com.amf.report.service.ExcelService;
import com.amf.report.service.HttpService;
import com.amf.report.service.ProductGroupReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.amf.report.constant.AppConstants.CHARACTERISTIC_CIDN;
import static com.amf.report.constant.AppConstants.EXCEL_FILE_EXTENSION;
import static com.amf.report.constant.AppConstants.EXCEL_FILE_TIMESTAMP_PATTERN;
import static com.amf.report.constant.AppConstants.ORGANIZATION_FIELDS;
import static com.amf.report.constant.AppConstants.ORGANIZATION_LIST;
import static com.amf.report.constant.AppConstants.ORGANIZATION_REPORT_NAME;
import static com.amf.report.constant.AppConstants.PRODUCT_GROUP_ID_PATTERN;
import static com.amf.report.constant.AppConstants.PRODUCT_GROUP_ID_VARIABLE;
import static com.amf.report.constant.AppConstants.QUERY_PARAM_FIELDS;
import static com.amf.report.constant.AppConstants.QUERY_PARAM_ID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductGroupReportServiceImpl implements ProductGroupReportService {
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern(EXCEL_FILE_TIMESTAMP_PATTERN);

    private final HttpService httpService;
    private final ExcelService excelService;
    private final ProductGroupApiProperties apiProperties;

    @Override
    public ProductGroupReportResponseDto generateAndSaveOrganizationReport(String environment, String productGroupId) {
        OrganizationReportFile report = createOrganizationReport(environment, productGroupId);
        Path file = excelService.saveReport(report.fileName(), report.content());
        return new ProductGroupReportResponseDto(
                report.environment().getLabel(),
                report.productGroupId(),
                report.productGroupName(),
                file.getFileName().toString(),
                file.toAbsolutePath().toString(),
                report.recordCount(),
                report.notFoundCount());
    }

    OrganizationReportFile createOrganizationReport(String environment, String productGroupId) {
        ReportEnvironment env = ReportEnvironment.fromValue(environment);
        String groupId = validateProductGroupId(productGroupId);
        String baseUrl = apiProperties.baseUrl(env);

        log.info("Generating organization report: environment={}, productGroupId={}", env.getLabel(), groupId);

        ProductGroupDto productGroup = loadProductGroup(baseUrl, groupId);
        List<String> addedCustomers = Optional.ofNullable(productGroup.getAddedCustomers()).orElse(Collections.emptyList());
        log.info("Product group '{}' has {} addedCustomers", productGroup.getName(), addedCustomers.size());

        Map<String, OrganizationDto> organizationById = loadOrganizations(baseUrl, addedCustomers);

        List<OrganizationReportRecord> records = addedCustomers.stream()
                .map(customerId -> toRecord(customerId, organizationById))
                .toList();

        List<String> notFound = addedCustomers.stream()
                .filter(id -> !StringUtils.hasText(id) || !organizationById.containsKey(id.trim()))
                .toList();
        if (!notFound.isEmpty()) {
            log.warn("{} addedCustomers were not returned by the organization API: {}", notFound.size(), notFound);
        }

        byte[] content = excelService.createOrganizationReport(records);
        return OrganizationReportFile.builder()
                .environment(env)
                .productGroupId(groupId)
                .productGroupName(productGroup.getName())
                .fileName(buildFileName(productGroup.getName(), env))
                .content(content)
                .recordCount(records.size())
                .notFoundCount(notFound.size())
                .build();
    }

    ProductGroupDto loadProductGroup(String baseUrl, String productGroupId) {
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .path(apiProperties.productGroupPath())
                .buildAndExpand(Map.of(PRODUCT_GROUP_ID_VARIABLE, productGroupId))
                .toUriString();

        ProductGroupDto productGroup = httpService.get(url, ProductGroupDto.class);
        if (productGroup == null) {
            throw new ResourceNotFoundException("Product group not found: " + productGroupId);
        }
        return productGroup;
    }

    Map<String, OrganizationDto> loadOrganizations(String baseUrl, List<String> customerIds) {
        List<String> uniqueIds = customerIds.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();

        Map<String, OrganizationDto> organizationById = new HashMap<>();
        if (uniqueIds.isEmpty()) {
            return organizationById;
        }

        List<List<String>> batches = uniqueIds.size() > apiProperties.organizationChunkThreshold()
                ? partition(uniqueIds, apiProperties.organizationChunkSize())
                : List.of(uniqueIds);

        for (int i = 0; i < batches.size(); i++) {
            List<String> batch = batches.get(i);
            String url = UriComponentsBuilder.fromUriString(baseUrl)
                    .path(apiProperties.organizationPath())
                    .queryParam(QUERY_PARAM_ID, batch.toArray())
                    .queryParam(QUERY_PARAM_FIELDS, ORGANIZATION_FIELDS.toArray())
                    .build()
                    .toUriString();

            List<OrganizationDto> organizations =
                    Optional.ofNullable(httpService.get(url, ORGANIZATION_LIST)).orElse(Collections.emptyList());
            organizations.stream()
                    .filter(Objects::nonNull)
                    .filter(org -> StringUtils.hasText(org.getId()))
                    .forEach(org -> organizationById.putIfAbsent(org.getId().trim(), org));

            log.info("Organization batch {}/{}: requested {}, returned {}",
                    i + 1, batches.size(), batch.size(), organizations.size());
        }
        return organizationById;
    }

    private OrganizationReportRecord toRecord(String customerId, Map<String, OrganizationDto> organizationById) {
        OrganizationDto organization = StringUtils.hasText(customerId) ? organizationById.get(customerId.trim()) : null;
        return OrganizationReportRecord.builder()
                .organizationId(customerId)
                .organizationName(organization == null ? null : organization.getName())
                .cidn(organization == null ? null : organization.getPartyCharacteristicValue(CHARACTERISTIC_CIDN))
                .build();
    }

    private static String validateProductGroupId(String productGroupId) {
        if (!StringUtils.hasText(productGroupId)) {
            throw new InvalidRequestException("productGroupId is required");
        }
        String trimmed = productGroupId.trim();
        if (!PRODUCT_GROUP_ID_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidRequestException(
                    "Invalid productGroupId '" + productGroupId + "'. Only letters, digits, '-' and '_' are allowed.");
        }
        return trimmed;
    }

    private static String buildFileName(String productGroupName, ReportEnvironment env) {
        String safeName = StringUtils.hasText(productGroupName)
                ? productGroupName.trim().replaceAll("[^A-Za-z0-9_-]+", "_")
                : "ProductGroup";
        return ORGANIZATION_REPORT_NAME + "_" + safeName + "_" + env.name() + "_"
                + LocalDateTime.now().format(FILE_TIMESTAMP) + EXCEL_FILE_EXTENSION;
    }

    private static <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> batches = new ArrayList<>();
        for (int from = 0; from < list.size(); from += size) {
            batches.add(list.subList(from, Math.min(from + size, list.size())));
        }
        return batches;
    }
}
