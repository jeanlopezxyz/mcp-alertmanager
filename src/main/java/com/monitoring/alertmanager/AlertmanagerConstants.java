package com.monitoring.alertmanager;

/**
 * Constants for Alertmanager MCP.
 */
public final class AlertmanagerConstants {

    private AlertmanagerConstants() {
        // Utility class
    }

    // Input limits
    public static final int MAX_NAME_LENGTH = 253;

    // Silence defaults
    public static final String DEFAULT_SILENCE_DURATION = "2h";
    public static final int DEFAULT_SILENCE_HOURS = 2;
    public static final String DEFAULT_SILENCE_COMMENT = "Created via MCP";
    public static final String DEFAULT_SILENCE_CREATOR = "mcp-alertmanager";

    // Result limits
    public static final int TOP_RESULTS_LIMIT = 5;
}
