package com.monitoring.alertmanager.mcp;

import com.monitoring.alertmanager.application.service.AlertmanagerService;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import static com.monitoring.alertmanager.AlertmanagerConstants.DEFAULT_SILENCE_COMMENT;
import static com.monitoring.alertmanager.AlertmanagerConstants.DEFAULT_SILENCE_CREATOR;
import static com.monitoring.alertmanager.AlertmanagerConstants.DEFAULT_SILENCE_DURATION;
import static com.monitoring.alertmanager.AlertmanagerConstants.MAX_NAME_LENGTH;

/**
 * MCP Tools for Prometheus Alertmanager operations.
 */
@ApplicationScoped
public class AlertmanagerTools {

    private static final Logger LOG = Logger.getLogger(AlertmanagerTools.class);

    @Inject
    AlertmanagerService alertmanagerService;

    @Tool(description = "Get alerts from Alertmanager. Returns active alerts by default. "
            + "Filter by: active, silenced, inhibited, or label (e.g., 'severity=critical').")
    public String getAlerts(
            @ToolArg(description = "Include active alerts") Boolean active,
            @ToolArg(description = "Include silenced alerts") Boolean silenced,
            @ToolArg(description = "Include inhibited alerts") Boolean inhibited,
            @ToolArg(description = "Label filter: 'key=value'") String filterLabel) {
        try {
            boolean noFilters = active == null && silenced == null
                    && inhibited == null && (filterLabel == null || filterLabel.isBlank());
            if (noFilters) {
                return alertmanagerService.getActiveAlerts();
            }
            return alertmanagerService.getAlerts(active, silenced, inhibited, filterLabel);
        } catch (Exception e) {
            LOG.errorf("Get alerts failed: %s", e.getMessage());
            return formatError("Failed to get alerts", e);
        }
    }

    @Tool(description = "Get alerts grouped by routing labels. Shows how alerts are batched for notifications.")
    public String getAlertGroups() {
        try {
            return alertmanagerService.getAlertGroups();
        } catch (Exception e) {
            LOG.errorf("Get alert groups failed: %s", e.getMessage());
            return formatError("Failed to get alert groups", e);
        }
    }

    @Tool(description = "List silences. Filter by state: 'active', 'pending', 'expired', or omit for all.")
    public String getSilences(@ToolArg(description = "State: 'active', 'pending', 'expired'") String state) {
        try {
            String validState = parseState(state, "active", "pending", "expired");
            return alertmanagerService.getSilences(validState);
        } catch (Exception e) {
            LOG.errorf("Get silences failed: %s", e.getMessage());
            return formatError("Failed to get silences", e);
        }
    }

    @Tool(description = "Create a silence for an alert. Duration format: '30m', '2h', '1d'. Max 30 days.")
    public String createSilence(
            @ToolArg(description = "Alert name to silence") String alertName,
            @ToolArg(description = "Duration: '30m', '2h', '1d'") String duration,
            @ToolArg(description = "Reason for silence") String comment,
            @ToolArg(description = "Creator name") String createdBy) {
        if (alertName == null || alertName.isBlank()) {
            return "Error: alertName is required";
        }
        if (alertName.length() > MAX_NAME_LENGTH) {
            return "Error: alertName too long (max " + MAX_NAME_LENGTH + " chars)";
        }

        try {
            String validDuration = (duration == null || duration.isBlank()) ? DEFAULT_SILENCE_DURATION : duration.trim();
            String validComment = (comment == null || comment.isBlank()) ? DEFAULT_SILENCE_COMMENT : comment.trim();
            String validCreator = (createdBy == null || createdBy.isBlank()) ? DEFAULT_SILENCE_CREATOR : createdBy.trim();
            return alertmanagerService.createSilence(alertName.trim(), validDuration, validComment, validCreator);
        } catch (Exception e) {
            LOG.errorf("Create silence failed: %s", e.getMessage());
            return formatError("Failed to create silence", e);
        }
    }

    @Tool(description = "Delete a silence by ID. Get ID from getSilences output.")
    public String deleteSilence(@ToolArg(description = "Silence UUID") String silenceId) {
        if (silenceId == null || silenceId.isBlank()) {
            return "Error: silenceId is required";
        }

        try {
            return alertmanagerService.deleteSilence(silenceId.trim());
        } catch (Exception e) {
            LOG.errorf("Delete silence failed: %s", e.getMessage());
            return formatError("Failed to delete silence", e);
        }
    }

    @Tool(description = "Get Alertmanager server status: version, uptime, cluster info.")
    public String getAlertmanagerStatus() {
        try {
            return alertmanagerService.getStatus();
        } catch (Exception e) {
            LOG.errorf("Get status failed: %s", e.getMessage());
            return formatError("Failed to get status", e);
        }
    }

    @Tool(description = "List configured notification receivers (Slack, email, PagerDuty, etc.).")
    public String getReceivers() {
        try {
            return alertmanagerService.getReceivers();
        } catch (Exception e) {
            LOG.errorf("Get receivers failed: %s", e.getMessage());
            return formatError("Failed to get receivers", e);
        }
    }

    @Tool(description = "Get alerting summary: counts by severity, top alerts, affected namespaces.")
    public String getAlertingSummary() {
        try {
            return alertmanagerService.getAlertingSummary();
        } catch (Exception e) {
            LOG.errorf("Get summary failed: %s", e.getMessage());
            return formatError("Failed to get alerting summary", e);
        }
    }

    @Tool(description = "Get critical severity alerts only. Prioritized for incident response.")
    public String getCriticalAlerts() {
        try {
            return alertmanagerService.getCriticalAlerts();
        } catch (Exception e) {
            LOG.errorf("Get critical alerts failed: %s", e.getMessage());
            return formatError("Failed to get critical alerts", e);
        }
    }

    @Tool(description = "Investigate an alert: all instances, duration, labels, silences, recommendations.")
    public String investigateAlert(@ToolArg(description = "Alert name to investigate") String alertName) {
        if (alertName == null || alertName.isBlank()) {
            return "Error: alertName is required";
        }
        if (alertName.length() > MAX_NAME_LENGTH) {
            return "Error: alertName too long (max " + MAX_NAME_LENGTH + " chars)";
        }

        try {
            return alertmanagerService.investigateAlert(alertName.trim());
        } catch (Exception e) {
            LOG.errorf("Investigate alert failed: %s", e.getMessage());
            return formatError("Failed to investigate alert", e);
        }
    }

    private String parseState(String state, String... validStates) {
        if (state == null || state.isBlank()) {
            return null;
        }
        String normalized = state.trim().toLowerCase();
        for (String valid : validStates) {
            if (valid.equals(normalized)) {
                return normalized;
            }
        }
        return null;
    }

    private String formatError(String message, Exception e) {
        String detail = e.getMessage();
        return (detail == null || detail.isBlank())
                ? "Error: " + message
                : "Error: " + message + " - " + detail;
    }
}
