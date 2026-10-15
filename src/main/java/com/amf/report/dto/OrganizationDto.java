package com.amf.report.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
