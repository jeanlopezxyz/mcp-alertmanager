package com.monitoring.alertmanager.infrastructure.dto;

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
        boolean isRegex,
        boolean isEqual
    ) {}
}
