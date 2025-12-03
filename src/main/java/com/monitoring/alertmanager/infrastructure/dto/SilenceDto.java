package com.monitoring.alertmanager.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SilenceDto(
    String id,
    StatusDto status,
    String updatedAt,
    String comment,
    String createdBy,
    String endsAt,
    String startsAt,
    List<MatcherDto> matchers
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StatusDto(
        String state
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MatcherDto(
        String name,
        String value,
        boolean isRegex,
        boolean isEqual
    ) {}
}
