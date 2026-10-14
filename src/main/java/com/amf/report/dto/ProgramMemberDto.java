package com.amf.report.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * TMF LoyaltyProgramMember. {@code name} is the organization name and the
 * characteristic value is the CIDN.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProgramMemberDto {

    private String id;

    private String name;

    private List<CharacteristicDto> characteristic;
}
