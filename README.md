# Alertmanager MCP Server

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![npm version](https://img.shields.io/npm/v/mcp-alertmanager)](https://www.npmjs.com/package/mcp-alertmanager)
[![Java](https://img.shields.io/badge/Java-21+-orange)](https://adoptium.net/)
[![GitHub release](https://img.shields.io/github/v/release/jeanlopezxyz/mcp-alertmanager)](https://github.com/jeanlopezxyz/mcp-alertmanager/releases/latest)

A [Model Context Protocol (MCP)](https://modelcontextprotocol.io/) server for Prometheus Alertmanager integration.

Built with [Quarkus MCP Server](https://docs.quarkiverse.io/quarkus-mcp-server/dev/index.html).

## Transport Modes

| Mode | Description | Use Case |
|------|-------------|----------|
| **stdio** | Standard input/output | Default for Claude Code, Claude Desktop, Cursor, VS Code |
| **SSE** | Server-Sent Events over HTTP | Standalone server, web integrations, multiple clients |

---

## Requirements

- **Java 21+** - [Download](https://adoptium.net/)
- **Prometheus Alertmanager** - Running and accessible

---

## Installation

### Claude Code

Add to `~/.claude/settings.json`:

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

### Claude Desktop

Add to `claude_desktop_config.json`:

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

### Cursor / VS Code

Add to your MCP configuration:

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

### SSE Mode

Run as standalone server:

```bash
ALERTMANAGER_URL="http://localhost:9093" npx mcp-alertmanager --port 9082
```

Endpoint: `http://localhost:9082/mcp/sse`

---

## Configuration

### Command Line Options

| Option | Description |
|--------|-------------|
| `--port <PORT>` | Start in SSE mode on specified port |
| `--help` | Show help message |
| `--version` | Show version |

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `ALERTMANAGER_URL` | Alertmanager API URL | `http://localhost:9093` |

---

## Tools

This server provides **7 tools** organized in 3 categories:

### Alerts

#### `getAlerts`
Get alerts from Alertmanager. Without filters, returns active/firing alerts.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `active` | boolean | No | Show active alerts (default if no filters) |
| `silenced` | boolean | No | Show silenced alerts |
| `inhibited` | boolean | No | Show inhibited alerts |
| `filterLabel` | string | No | Filter by label (e.g., `severity=critical`) |

**Examples:**
- Active alerts: `getAlerts` (no parameters)
- Critical alerts: `getAlerts filterLabel='severity=critical'`
- Silenced alerts: `getAlerts silenced=true`

---

#### `getAlertGroups`
Get alerts grouped by their routing labels. Shows how Alertmanager batches alerts for notifications.

**Parameters:** None

**Returns:** Alert groups with receiver and alert counts.

---

### Silences

#### `getSilences`
Get silences from Alertmanager. Silences suppress notifications for matching alerts.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `state` | string | No | Filter: `active`, `pending`, or `expired` |

**Returns:** List of silences with matchers, expiration, and creator.

---

#### `createSilence`
Create a silence to suppress notifications for an alert.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `alertName` | string | Yes | Alert name to silence (exact match) |
| `duration` | string | No | Duration: `30m`, `2h`, `1d` (default: `2h`) |
| `comment` | string | No | Reason for silencing |
| `createdBy` | string | No | Your name/identifier |

**Examples:**
- Silence for 2 hours: `createSilence alertName='HighMemoryUsage'`
- Silence for maintenance: `createSilence alertName='DiskSpaceLow' duration='1d' comment='Scheduled maintenance'`

---

#### `deleteSilence`
Delete/expire a silence by ID. The alert will resume firing notifications if still active.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `silenceId` | string | Yes | Silence ID to delete |

---

### Status

#### `getAlertmanagerStatus`
Get Alertmanager cluster status: version, uptime, cluster peers, and configuration.

**Parameters:** None

---

#### `getReceivers`
Get configured notification receivers (email, Slack, PagerDuty, webhooks).

**Parameters:** None

---

## Example Prompts

Use natural language to interact with Alertmanager. Here are prompts organized by use case:

### Checking Alerts

```
"What alerts are currently firing?"
"Show me all active alerts"
"Are there any critical severity alerts?"
"What alerts are firing in the production namespace?"
"Show alerts from the platform team"
"Do we have any high severity alerts right now?"
"What's currently broken in my infrastructure?"
```

### Investigating Specific Issues

```
"Show me all alerts related to memory"
"Are there any disk space alerts?"
"What database alerts are firing?"
"Show alerts for the payment service"
"Are there any network-related alerts?"
"What Kubernetes pod alerts do we have?"
```

### Managing Silences

```
"What silences are currently active?"
"Show me all silences"
"Who silenced the HighMemoryUsage alert?"
"Are there any expired silences?"
"Create a 2-hour silence for HighCPUUsage - scheduled maintenance"
"Silence the DiskSpaceLow alert for 4 hours, database cleanup in progress"
"Create a 30-minute silence for PodCrashLooping while I investigate"
"Delete silence abc-123-def-456"
"Remove the silence on HighMemoryUsage"
```

### Understanding Alert Routing

```
"How are alerts grouped?"
"Show me alert groups"
"What receivers are configured?"
"Where do critical alerts get sent?"
"Show me all notification channels"
"Which Slack channels receive alerts?"
```

### Status and Health

```
"Is Alertmanager running?"
"What version of Alertmanager are we using?"
"Show Alertmanager cluster status"
"Are all Alertmanager peers connected?"
```

### Combined Workflows

```
"Show me critical alerts and then silence the noisy ones for 1 hour"
"What's firing? I need to understand the current incident"
"List all alerts, their silences, and the notification receivers"
"Give me a complete overview of our alerting status"
```

---

## Development

### Run in dev mode

```bash
export ALERTMANAGER_URL="http://localhost:9093"
./mvnw quarkus:dev
```

### Build

```bash
./mvnw package -DskipTests
```

### Test with MCP Inspector

```bash
# stdio mode
ALERTMANAGER_URL="http://localhost:9093" npx @modelcontextprotocol/inspector npx mcp-alertmanager

# SSE mode
ALERTMANAGER_URL="http://localhost:9093" npx mcp-alertmanager --port 9082
# Then connect inspector to http://localhost:9082/mcp/sse
```

---

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## License

MIT
