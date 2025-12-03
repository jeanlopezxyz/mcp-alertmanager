package com.monitoring.alertmanager.application.service;

import com.monitoring.alertmanager.infrastructure.client.AlertmanagerClient;
import com.monitoring.alertmanager.infrastructure.dto.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class AlertmanagerService {

    private static final Logger LOG = Logger.getLogger(AlertmanagerService.class);

    @Inject
    @RestClient
    AlertmanagerClient alertmanagerClient;

    /**
     * Get all active alerts.
     */
    public String getActiveAlerts() {
        LOG.info("Getting active alerts");
        try {
            List<AlertDto> alerts = alertmanagerClient.getAlerts(true, false, false, false, null, null);
            return formatAlerts(alerts, "Active Alerts");
        } catch (Exception e) {
            LOG.errorf("Error getting active alerts: %s", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Get all alerts with optional filters.
     */
    public String getAlerts(Boolean active, Boolean silenced, Boolean inhibited, String filterLabel) {
        LOG.infof("Getting alerts - active: %s, silenced: %s, inhibited: %s", active, silenced, inhibited);
        try {
            List<String> filter = filterLabel != null ? List.of(filterLabel) : null;
            List<AlertDto> alerts = alertmanagerClient.getAlerts(active, silenced, inhibited, null, filter, null);
            return formatAlerts(alerts, "Alerts");
        } catch (Exception e) {
            LOG.errorf("Error getting alerts: %s", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Get alerts grouped by labels.
     */
    public String getAlertGroups() {
        LOG.info("Getting alert groups");
        try {
            List<AlertGroupDto> groups = alertmanagerClient.getAlertGroups(null, null, null, null, null);
            return formatAlertGroups(groups);
        } catch (Exception e) {
            LOG.errorf("Error getting alert groups: %s", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Get all silences.
     */
    public String getSilences(String state) {
        LOG.infof("Getting silences with state: %s", state);
        try {
            List<SilenceDto> silences = alertmanagerClient.getSilences(null);

            // Filter by state if specified
            if (state != null && !state.isEmpty()) {
                silences = silences.stream()
                    .filter(s -> s.status() != null && state.equalsIgnoreCase(s.status().state()))
                    .collect(Collectors.toList());
            }

            return formatSilences(silences);
        } catch (Exception e) {
            LOG.errorf("Error getting silences: %s", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Create a silence for an alert.
     */
    public String createSilence(String alertName, String duration, String comment, String createdBy) {
        LOG.infof("Creating silence for alert: %s, duration: %s", alertName, duration);
        try {
            Instant now = Instant.now();
            Instant endTime = parseDuration(now, duration);

            CreateSilenceDto silence = new CreateSilenceDto(
                comment != null ? comment : "Created via MCP",
                createdBy != null ? createdBy : "mcp-alertmanager",
                endTime.toString(),
                now.toString(),
                List.of(new CreateSilenceDto.MatcherDto("alertname", alertName, false, true))
            );

            SilenceDto created = alertmanagerClient.createSilence(silence);
            return String.format("Silence created successfully!\nID: %s\nExpires: %s", created.id(), endTime);
        } catch (Exception e) {
            LOG.errorf("Error creating silence: %s", e.getMessage());
            return "Error creating silence: " + e.getMessage();
        }
    }

    /**
     * Delete a silence by ID.
     */
    public String deleteSilence(String silenceId) {
        LOG.infof("Deleting silence: %s", silenceId);
        try {
            alertmanagerClient.deleteSilence(silenceId);
            return String.format("Silence %s deleted successfully!", silenceId);
        } catch (Exception e) {
            LOG.errorf("Error deleting silence: %s", e.getMessage());
            return "Error deleting silence: " + e.getMessage();
        }
    }

    /**
     * Get Alertmanager status.
     */
    public String getStatus() {
        LOG.info("Getting Alertmanager status");
        try {
            Object status = alertmanagerClient.getStatus();
            return "Alertmanager Status:\n" + status.toString();
        } catch (Exception e) {
            LOG.errorf("Error getting status: %s", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Get configured receivers.
     */
    public String getReceivers() {
        LOG.info("Getting receivers");
        try {
            List<Object> receivers = alertmanagerClient.getReceivers();
            StringBuilder sb = new StringBuilder();
            sb.append("=== Configured Receivers ===\n\n");
            for (Object receiver : receivers) {
                sb.append("- ").append(receiver).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            LOG.errorf("Error getting receivers: %s", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    // =========================================================================
    // Private Helper Methods
    // =========================================================================

    private Instant parseDuration(Instant from, String duration) {
        if (duration == null || duration.isEmpty()) {
            return from.plus(2, ChronoUnit.HOURS);
        }

        String value = duration.replaceAll("[^0-9]", "");
        String unit = duration.replaceAll("[0-9]", "").toLowerCase();
        int amount = Integer.parseInt(value);

        return switch (unit) {
            case "m" -> from.plus(amount, ChronoUnit.MINUTES);
            case "h" -> from.plus(amount, ChronoUnit.HOURS);
            case "d" -> from.plus(amount, ChronoUnit.DAYS);
            default -> from.plus(2, ChronoUnit.HOURS);
        };
    }

    private String formatAlerts(List<AlertDto> alerts, String title) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(title).append(" (").append(alerts.size()).append(") ===\n\n");

        if (alerts.isEmpty()) {
            sb.append("No alerts found.\n");
            return sb.toString();
        }

        for (AlertDto alert : alerts) {
            Map<String, String> labels = alert.labels();
            sb.append("Alert: ").append(labels.getOrDefault("alertname", "Unknown")).append("\n");
            sb.append("Severity: ").append(labels.getOrDefault("severity", "unknown")).append("\n");
            sb.append("State: ").append(alert.status() != null ? alert.status().state() : "unknown").append("\n");
            sb.append("Started: ").append(alert.startsAt()).append("\n");

            if (alert.annotations() != null) {
                String summary = alert.annotations().get("summary");
                if (summary != null) {
                    sb.append("Summary: ").append(summary).append("\n");
                }
                String description = alert.annotations().get("description");
                if (description != null) {
                    sb.append("Description: ").append(description).append("\n");
                }
            }

            // Show if silenced
            if (alert.status() != null && alert.status().silencedBy() != null && !alert.status().silencedBy().isEmpty()) {
                sb.append("Silenced By: ").append(String.join(", ", alert.status().silencedBy())).append("\n");
            }

            sb.append("Labels: ").append(labels).append("\n");
            sb.append("\n");
        }

        return sb.toString();
    }

    private String formatAlertGroups(List<AlertGroupDto> groups) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Alert Groups (").append(groups.size()).append(") ===\n\n");

        if (groups.isEmpty()) {
            sb.append("No alert groups found.\n");
            return sb.toString();
        }

        for (AlertGroupDto group : groups) {
            sb.append("Group: ").append(group.labels()).append("\n");
            sb.append("Receiver: ").append(group.receiver() != null ? group.receiver().name() : "unknown").append("\n");
            sb.append("Alerts: ").append(group.alerts() != null ? group.alerts().size() : 0).append("\n");

            if (group.alerts() != null && !group.alerts().isEmpty()) {
                for (AlertDto alert : group.alerts()) {
                    String alertname = alert.labels() != null ? alert.labels().get("alertname") : "unknown";
                    String state = alert.status() != null ? alert.status().state() : "unknown";
                    sb.append("  - ").append(alertname).append(" (").append(state).append(")\n");
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    private String formatSilences(List<SilenceDto> silences) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Silences (").append(silences.size()).append(") ===\n\n");

        if (silences.isEmpty()) {
            sb.append("No silences found.\n");
            return sb.toString();
        }

        for (SilenceDto silence : silences) {
            sb.append("ID: ").append(silence.id()).append("\n");
            sb.append("State: ").append(silence.status() != null ? silence.status().state() : "unknown").append("\n");
            sb.append("Created By: ").append(silence.createdBy()).append("\n");
            sb.append("Comment: ").append(silence.comment()).append("\n");
            sb.append("Starts: ").append(silence.startsAt()).append("\n");
            sb.append("Ends: ").append(silence.endsAt()).append("\n");

            if (silence.matchers() != null && !silence.matchers().isEmpty()) {
                sb.append("Matchers:\n");
                for (var matcher : silence.matchers()) {
                    String op = matcher.isEqual() ? "=" : "!=";
                    if (matcher.isRegex()) op = matcher.isEqual() ? "=~" : "!~";
                    sb.append("  ").append(matcher.name()).append(op).append(matcher.value()).append("\n");
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
