package com.monitoring.alertmanager.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for create silence operation.
 * Alertmanager API v2 returns {"silenceID": "xxx"} not the full silence object.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateSilenceResponseDto(
    @JsonProperty("silenceID") String silenceID
) {}
