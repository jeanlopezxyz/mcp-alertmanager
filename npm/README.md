# mcp-alertmanager

MCP Server for Prometheus Alertmanager. Monitor alerts, manage silences, and understand notification routing through AI assistants.

## Quick Start

```bash
ALERTMANAGER_URL="http://localhost:9093" npx mcp-alertmanager
```

## Configuration

Add to `~/.claude/settings.json` (Claude Code) or your MCP client config:

```json
{
  "mcpServers": {
    "alertmanager": {
      "command": "npx",
      "args": ["-y", "mcp-alertmanager@latest"],
      "env": {
        "ALERTMANAGER_URL": "http://localhost:9093"
      }
    }
  }
}
```

## Requirements

- Java 21+
- Prometheus Alertmanager running and accessible

## Tools (7)

| Tool | Description |
|------|-------------|
| `getAlerts` | Get firing alerts, filter by severity/namespace/state |
| `getAlertGroups` | See how alerts are grouped for notifications |
| `getSilences` | List active, pending, or expired silences |
| `createSilence` | Silence an alert during maintenance |
| `deleteSilence` | Remove a silence to resume notifications |
| `getAlertmanagerStatus` | Check server health and cluster status |
| `getReceivers` | List notification channels (Slack, email, etc.) |

## Example Prompts

```
"What alerts are currently firing?"
"Are there any critical alerts in production?"
"Silence HighMemoryUsage for 2 hours - maintenance"
"Show me all active silences"
"What notification receivers are configured?"
"Give me an overview of the alerting status"
```

## Documentation

Full docs: https://github.com/jeanlopezxyz/mcp-alertmanager

## License

MIT
