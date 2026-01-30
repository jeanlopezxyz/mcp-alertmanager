package com.monitoring.alertmanager.application.service;

import com.monitoring.alertmanager.infrastructure.client.AlertmanagerClient;
import com.monitoring.alertmanager.infrastructure.dto.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.monitoring.alertmanager.AlertmanagerConstants.DEFAULT_SILENCE_HOURS;
import static com.monitoring.alertmanager.AlertmanagerConstants.DEFAULT_SILENCE_COMMENT;
import static com.monitoring.alertmanager.AlertmanagerConstants.DEFAULT_SILENCE_CREATOR;
import static com.monitoring.alertmanager.AlertmanagerConstants.TOP_RESULTS_LIMIT;

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
            List<String> filter = (filterLabel != null && !filterLabel.isEmpty()) ? List.of(filterLabel) : null;
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
                    .toList();
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
                comment != null ? comment : DEFAULT_SILENCE_COMMENT,
                createdBy != null ? createdBy : DEFAULT_SILENCE_CREATOR,
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
    // Intelligent Troubleshooting Tools
    // =========================================================================

    /**
     * Get executive summary of all alerts.
     * Shows counts by severity, highlights critical issues, identifies trends.
     */
    public String getAlertingSummary() {
        LOG.info("Getting alerting summary");
        try {
            List<AlertDto> alerts = alertmanagerClient.getAlerts(true, false, false, false, null, null);
            List<AlertDto> silencedAlerts = alertmanagerClient.getAlerts(false, true, false, false, null, null);

            // Group by severity
            Map<String, Long> bySeverity = alerts.stream()
                .collect(Collectors.groupingBy(
                    a -> a.labels().getOrDefault("severity", "unknown"),
                    Collectors.counting()
                ));

            // Group by namespace
            Map<String, Long> byNamespace = alerts.stream()
                .collect(Collectors.groupingBy(
                    a -> a.labels().getOrDefault("namespace", "cluster-wide"),
                    Collectors.counting()
                ));

            // Find top alert names
            Map<String, Long> byAlertName = alerts.stream()
                .collect(Collectors.groupingBy(
                    a -> a.labels().getOrDefault("alertname", "unknown"),
                    Collectors.counting()
                ));

            // Sort by count descending
            List<Map.Entry<String, Long>> topAlerts = byAlertName.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(TOP_RESULTS_LIMIT)
                .toList();

            List<Map.Entry<String, Long>> topNamespaces = byNamespace.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(TOP_RESULTS_LIMIT)
                .toList();

            // Build summary
            StringBuilder sb = new StringBuilder();
            sb.append("=== Alerting Summary ===\n\n");

            // Total counts
            sb.append("OVERVIEW\n");
            sb.append("  Total Active: ").append(alerts.size()).append("\n");
            sb.append("  Silenced: ").append(silencedAlerts.size()).append("\n\n");

            // By severity
            sb.append("BY SEVERITY\n");
            sb.append("  Critical: ").append(bySeverity.getOrDefault("critical", 0L)).append("\n");
            sb.append("  Warning:  ").append(bySeverity.getOrDefault("warning", 0L)).append("\n");
            sb.append("  Info:     ").append(bySeverity.getOrDefault("info", 0L)).append("\n");
            sb.append("  None:     ").append(bySeverity.getOrDefault("none", 0L)).append("\n\n");

            // Top alerts
            sb.append("TOP FIRING ALERTS\n");
            for (Map.Entry<String, Long> entry : topAlerts) {
                sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            sb.append("\n");

            // Top namespaces
            sb.append("TOP AFFECTED NAMESPACES\n");
            for (Map.Entry<String, Long> entry : topNamespaces) {
                sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }

            // Action recommendations
            if (bySeverity.getOrDefault("critical", 0L) > 0) {
                sb.append("\n⚠ ATTENTION: ").append(bySeverity.get("critical")).append(" critical alert(s) firing!\n");
                sb.append("Use getCriticalAlerts for details.\n");
            }

            return sb.toString();
        } catch (Exception e) {
            LOG.errorf("Error getting alerting summary: %s", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Get only critical severity alerts with full context.
     * Prioritized view for incident response.
     */
    public String getCriticalAlerts() {
        LOG.info("Getting critical alerts");
        try {
            List<AlertDto> alerts = alertmanagerClient.getAlerts(
                true, false, false, false,
                List.of("severity=critical"), null
            );

            if (alerts.isEmpty()) {
                return "=== Critical Alerts ===\n\nNo critical alerts firing. System healthy.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("=== Critical Alerts (").append(alerts.size()).append(") ===\n\n");
            sb.append("⚠ IMMEDIATE ATTENTION REQUIRED ⚠\n\n");

            for (AlertDto alert : alerts) {
                Map<String, String> labels = alert.labels();
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                sb.append("ALERT: ").append(labels.getOrDefault("alertname", "Unknown")).append("\n");
                sb.append("Duration: ").append(formatDurationFromTimestamp(alert.startsAt())).append("\n");
                appendAlertAnnotations(sb, alert.annotations(), "");
                sb.append("Affected Resources:\n");
                appendAffectedResources(sb, labels, "  ");
                sb.append("\n");
            }

            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            sb.append("\nUse investigateAlert(alertName) for deeper investigation.");

            return sb.toString();
        } catch (Exception e) {
            LOG.errorf("Error getting critical alerts: %s", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Deep investigation of a specific alert.
     * Shows full details, duration, affected resources, related silences.
     */
    public String investigateAlert(String alertName) {
        LOG.infof("Investigating alert: %s", alertName);
        try {
            // Get all instances of this alert (active, silenced, inhibited)
            List<AlertDto> activeAlerts = alertmanagerClient.getAlerts(
                true, false, false, false,
                List.of("alertname=" + alertName), null
            );
            List<AlertDto> silencedAlerts = alertmanagerClient.getAlerts(
                false, true, false, false,
                List.of("alertname=" + alertName), null
            );
            List<AlertDto> inhibitedAlerts = alertmanagerClient.getAlerts(
                false, false, true, false,
                List.of("alertname=" + alertName), null
            );

            List<AlertDto> allAlerts = new ArrayList<>();
            allAlerts.addAll(activeAlerts);
            allAlerts.addAll(silencedAlerts);
            allAlerts.addAll(inhibitedAlerts);

            if (allAlerts.isEmpty()) {
                return String.format("=== Alert Investigation: %s ===\n\nAlert not found or not firing.\n" +
                    "The alert may be inactive or the name may be incorrect.\n" +
                    "Use getAlerts to see all active alerts.", alertName);
            }

            StringBuilder sb = new StringBuilder();
            sb.append("=== Alert Investigation: ").append(alertName).append(" ===\n\n");

            // Summary
            sb.append("SUMMARY\n");
            sb.append("  Active Instances: ").append(activeAlerts.size()).append("\n");
            sb.append("  Silenced Instances: ").append(silencedAlerts.size()).append("\n");
            sb.append("  Inhibited Instances: ").append(inhibitedAlerts.size()).append("\n\n");

            // Get details from first alert
            AlertDto firstAlert = allAlerts.get(0);

            // Alert metadata
            sb.append("ALERT DETAILS\n");
            sb.append("  Severity: ").append(firstAlert.labels().getOrDefault("severity", "unknown")).append("\n");
            appendAlertAnnotations(sb, firstAlert.annotations(), "  ");
            sb.append("\n");

            // Each instance
            sb.append("INSTANCES\n");
            for (int i = 0; i < allAlerts.size(); i++) {
                AlertDto alert = allAlerts.get(i);
                sb.append("\n--- Instance ").append(i + 1).append(" ---\n");

                // State
                String state = alert.status() != null ? alert.status().state() : "unknown";
                sb.append("State: ").append(state).append("\n");
                sb.append("Duration: ").append(formatDurationFromTimestamp(alert.startsAt())).append("\n");
                if (alert.startsAt() != null) {
                    sb.append("Started: ").append(alert.startsAt()).append("\n");
                }

                // Labels (affected resources)
                sb.append("Labels:\n");
                appendAffectedResources(sb, alert.labels(), "  ");

                // Silences
                if (alert.status() != null && alert.status().silencedBy() != null && !alert.status().silencedBy().isEmpty()) {
                    sb.append("Silenced By:\n");
                    for (String silenceId : alert.status().silencedBy()) {
                        sb.append("  - ").append(silenceId).append("\n");
                    }
                }

                // Inhibitions
                if (alert.status() != null && alert.status().inhibitedBy() != null && !alert.status().inhibitedBy().isEmpty()) {
                    sb.append("Inhibited By:\n");
                    for (String inhibitor : alert.status().inhibitedBy()) {
                        sb.append("  - ").append(inhibitor).append("\n");
                    }
                }
            }

            // Recommendations
            sb.append("\nRECOMMENDATIONS\n");
            if (!activeAlerts.isEmpty()) {
                sb.append("  - Use createSilence to temporarily suppress notifications during investigation\n");
            }
            if (firstAlert.annotations() != null && firstAlert.annotations().get("runbook_url") != null) {
                sb.append("  - Follow the runbook for resolution steps\n");
            }
            sb.append("  - Check Prometheus for related metrics using the labels above\n");

            return sb.toString();
        } catch (Exception e) {
            LOG.errorf("Error investigating alert: %s", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    // =========================================================================
    // Private Helper Methods
    // =========================================================================

    /**
     * Format duration from a timestamp string to human-readable format.
     */
    private String formatDurationFromTimestamp(String timestamp) {
        if (timestamp == null) {
            return "unknown";
        }
        try {
            Instant start = Instant.parse(timestamp);
            Duration duration = Duration.between(start, Instant.now());
            return formatDuration(duration);
        } catch (Exception e) {
            return timestamp;
        }
    }

    private String formatDuration(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0 || days > 0) {
            sb.append(hours).append("h ");
        }
        sb.append(minutes).append("m");
        return sb.toString();
    }

    /**
     * Append alert annotations (summary, description, runbook) to StringBuilder.
     */
    private void appendAlertAnnotations(StringBuilder sb, Map<String, String> annotations, String indent) {
        if (annotations == null) {
            return;
        }
        String summary = annotations.get("summary");
        if (summary != null) {
            sb.append(indent).append("Summary: ").append(summary).append("\n");
        }
        String description = annotations.get("description");
        if (description != null) {
            sb.append(indent).append("Description: ").append(description).append("\n");
        }
        String runbook = annotations.get("runbook_url");
        if (runbook != null) {
            sb.append(indent).append("Runbook: ").append(runbook).append("\n");
        }
    }

    /**
     * Append alert labels as affected resources to StringBuilder.
     */
    private void appendAffectedResources(StringBuilder sb, Map<String, String> labels, String indent) {
        labels.forEach((k, v) -> {
            if (!k.equals("alertname") && !k.equals("severity") && !k.equals("prometheus")) {
                sb.append(indent).append(k).append(": ").append(v).append("\n");
            }
        });
    }

    private Instant parseDuration(Instant from, String duration) {
        if (duration == null || duration.isEmpty()) {
            return from.plus(DEFAULT_SILENCE_HOURS, ChronoUnit.HOURS);
        }

        String value = duration.replaceAll("[^0-9]", "");
        String unit = duration.replaceAll("[0-9]", "").toLowerCase();

        if (value.isEmpty()) {
            return from.plus(DEFAULT_SILENCE_HOURS, ChronoUnit.HOURS);
        }

        int amount;
        try {
            amount = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return from.plus(DEFAULT_SILENCE_HOURS, ChronoUnit.HOURS);
        }

        return switch (unit) {
            case "m" -> from.plus(amount, ChronoUnit.MINUTES);
            case "h" -> from.plus(amount, ChronoUnit.HOURS);
            case "d" -> from.plus(amount, ChronoUnit.DAYS);
            default -> from.plus(DEFAULT_SILENCE_HOURS, ChronoUnit.HOURS);
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
            appendAlertAnnotations(sb, alert.annotations(), "");

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
