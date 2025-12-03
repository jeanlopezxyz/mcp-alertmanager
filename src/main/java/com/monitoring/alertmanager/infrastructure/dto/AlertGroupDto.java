package com.monitoring.alertmanager.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AlertGroupDto(
    Map<String, String> labels,
    ReceiverDto receiver,
    List<AlertDto> alerts
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ReceiverDto(
        String name
    ) {}
}
