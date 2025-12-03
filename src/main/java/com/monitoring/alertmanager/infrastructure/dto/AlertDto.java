package com.monitoring.alertmanager.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AlertDto(
    Map<String, String> labels,
    Map<String, String> annotations,
    String startsAt,
    String endsAt,
    String generatorURL,
    String fingerprint,
    StatusDto status,
    List<ReceiverDto> receivers
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StatusDto(
        String state,
        List<String> silencedBy,
        List<String> inhibitedBy
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ReceiverDto(
        String name
    ) {}
}
