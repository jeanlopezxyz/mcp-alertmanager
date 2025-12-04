package com.monitoring.alertmanager.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record CreateSilenceDto(
    String comment,
    String createdBy,
    String endsAt,
    String startsAt,
    List<MatcherDto> matchers
) {
    public record MatcherDto(
        String name,
        String value,
        @JsonProperty("isRegex") boolean isRegex,
        @JsonProperty("isEqual") boolean isEqual
    ) {}
}
