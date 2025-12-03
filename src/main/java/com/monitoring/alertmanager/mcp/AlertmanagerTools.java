package com.monitoring.alertmanager.mcp;

import com.monitoring.alertmanager.application.service.AlertmanagerService;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * MCP Tools for Prometheus Alertmanager operations.
 *
 * This server enables AI assistants to monitor, manage, and respond to alerts
 * in your infrastructure. Use it to check what's firing, silence noisy alerts
 * during maintenance, and understand your notification routing.
 *
 * Provides 7 tools in 3 categories:
 * - Alerts: getAlerts, getAlertGroups
 * - Silences: getSilences, createSilence, deleteSilence
 * - Status: getAlertmanagerStatus, getReceivers
 */
@ApplicationScoped
public class AlertmanagerTools {

    @Inject
    AlertmanagerService alertmanagerService;

    // =========================================================================
    // Alert Tools
    // =========================================================================

    @Tool(description = "Retrieve alerts from Alertmanager. Returns currently firing alerts by default. "
            + "Use this to: check what's wrong in your infrastructure, find critical issues, "
            + "identify silenced alerts, or filter alerts by severity/namespace/team. "
            + "Examples: 'show all alerts', 'what critical alerts are firing?', 'show silenced alerts in production'")
    public String getAlerts(
        @ToolArg(description = "Include active/firing alerts (default: true when no filters specified)") Boolean active,
        @ToolArg(description = "Include silenced alerts (notifications suppressed)") Boolean silenced,
        @ToolArg(description = "Include inhibited alerts (suppressed by other alerts)") Boolean inhibited,
        @ToolArg(description = "Label filter in format 'key=value'. Examples: 'severity=critical', 'namespace=production', 'team=platform'") String filterLabel
    ) {
        // If no filters specified, default to showing active alerts
        if (active == null && silenced == null && inhibited == null && filterLabel == null) {
            return alertmanagerService.getActiveAlerts();
        }
        return alertmanagerService.getAlerts(active, silenced, inhibited, filterLabel);
    }

    @Tool(description = "Get alerts organized by their routing groups. Shows how Alertmanager batches alerts "
            + "before sending to receivers. Use this to: understand alert grouping, see which alerts go together, "
            + "debug notification routing. Each group shares the same receiver and notification timing.")
    public String getAlertGroups() {
        return alertmanagerService.getAlertGroups();
    }

    // =========================================================================
    // Silence Tools
    // =========================================================================

    @Tool(description = "List silences that suppress alert notifications. Silences temporarily mute alerts "
            + "matching specific criteria. Use this to: see what's being silenced, check maintenance windows, "
            + "find who silenced an alert and why, review expired silences. "
            + "Filter by state: 'active' (currently suppressing), 'pending' (scheduled), 'expired' (past).")
    public String getSilences(
        @ToolArg(description = "Filter by silence state: 'active' (currently suppressing), 'pending' (future), 'expired' (past), or omit for all") String state
    ) {
        return alertmanagerService.getSilences(state);
    }

    @Tool(description = "Create a silence to temporarily suppress notifications for a specific alert. "
            + "Use this for: planned maintenance windows, known issues being worked on, noisy alerts during incidents. "
            + "The alert continues to fire but notifications are suppressed until the silence expires.")
    public String createSilence(
        @ToolArg(description = "Exact alert name to silence (matches alertname label). Example: 'HighMemoryUsage', 'PodCrashLooping'") String alertName,
        @ToolArg(description = "How long to silence: '30m' (30 minutes), '2h' (2 hours), '1d' (1 day). Default: 2h") String duration,
        @ToolArg(description = "Reason for silencing - be descriptive for teammates. Example: 'Scheduled DB maintenance', 'Known issue, fix in progress'") String comment,
        @ToolArg(description = "Who is creating this silence. Example: 'jsmith', 'oncall-team'") String createdBy
    ) {
        return alertmanagerService.createSilence(alertName, duration, comment, createdBy);
    }

    @Tool(description = "Delete/expire a silence immediately. The matched alerts will resume sending notifications "
            + "if still firing. Use this when: maintenance is complete early, issue was resolved, silence was created by mistake.")
    public String deleteSilence(
        @ToolArg(description = "The silence UUID to delete. Get this from getSilences tool output.") String silenceId
    ) {
        return alertmanagerService.deleteSilence(silenceId);
    }

    // =========================================================================
    // Status Tools
    // =========================================================================

    @Tool(description = "Get Alertmanager server status including version, uptime, and cluster information. "
            + "Use this to: verify Alertmanager is running, check cluster health in HA setups, "
            + "get version info for troubleshooting, see peer connectivity status.")
    public String getAlertmanagerStatus() {
        return alertmanagerService.getStatus();
    }

    @Tool(description = "List all configured notification receivers (where alerts are sent). "
            + "Shows email addresses, Slack channels, PagerDuty services, webhook URLs, etc. "
            + "Use this to: understand notification routing, verify receiver configuration, "
            + "see which teams receive which alerts.")
    public String getReceivers() {
        return alertmanagerService.getReceivers();
    }
}
