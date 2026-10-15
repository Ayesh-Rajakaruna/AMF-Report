package com.amf.report.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * One item of GET /tox-tx-layer-party-mgt/tmf-api/partyManagement/v5/organization?id=..&fields=name&fields=partyCharacteristic
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganizationDto {
    private String id;

    private String name;

    private List<CharacteristicDto> partyCharacteristic;

    public String getPartyCharacteristicValue(String characteristicName) {
        return CharacteristicDto.findValue(partyCharacteristic, characteristicName);
    }
}
