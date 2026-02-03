# Alertmanager MCP Server (Go)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![npm version](https://img.shields.io/npm/v/mcp-alertmanager)](https://www.npmjs.com/package/mcp-alertmanager)
[![Go](https://img.shields.io/badge/Go-1.25-00ADD8?logo=go)](https://go.dev/)
[![GitHub release](https://img.shields.io/github/v/release/jeanlopezxyz/mcp-alertmanager?sort=semver)](https://github.com/jeanlopezxyz/mcp-alertmanager/releases/latest)

A [Model Context Protocol (MCP)](https://modelcontextprotocol.io/) server for Prometheus Alertmanager integration. Native Go binary with built-in Kubernetes connectivity via client-go.

## Installation

### npx

```bash
npx -y mcp-alertmanager@latest
```

### MCP Client Configuration

Add to your MCP client configuration (VS Code, Cursor, Windsurf, etc.):

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

### VS Code

```shell
code --add-mcp '{"name":"alertmanager","command":"npx","args":["-y","mcp-alertmanager@latest"],"env":{"ALERTMANAGER_URL":"http://localhost:9093"}}'
```

### Kubernetes Auto-Connect

Connects directly to Alertmanager running in Kubernetes via the K8S API service proxy. Uses native kubeconfig/in-cluster config via client-go. No `kubectl` or port-forwarding required.

```json
{
  "mcpServers": {
    "alertmanager": {
      "command": "npx",
      "args": ["-y", "mcp-alertmanager@latest"],
      "env": {
        "K8S_NAMESPACE": "openshift-monitoring",
        "K8S_SERVICE": "alertmanager-main"
      }
    }
  }
}
```

### Binary

Download from [GitHub Releases](https://github.com/jeanlopezxyz/mcp-alertmanager/releases) or build from source:

```bash
make build
./mcp-alertmanager
```

---

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `ALERTMANAGER_URL` | Alertmanager API URL | `http://localhost:9093` |

### Kubernetes Auto-Connect Variables

| Variable | Required | Description | Default |
|----------|----------|-------------|---------|
| `K8S_NAMESPACE` | Yes | Kubernetes namespace of the service | - |
| `K8S_SERVICE` | Yes | Kubernetes service name | - |
| `K8S_SERVICE_PORT` | No | Service port | `9093` |
| `KUBECONFIG` | No | Path to kubeconfig file | `~/.kube/config` |

**Precedence:** `ALERTMANAGER_URL` > `K8S_NAMESPACE` + `K8S_SERVICE` > `http://localhost:9093`

**Connection strategy:**
1. In-cluster config (when running inside a pod)
2. Kubeconfig file (`KUBECONFIG` or `~/.kube/config`)

---

## Tools (12)

### Alerts

| Tool | Description |
|------|-------------|
| `getAlerts` | Get alerts with optional filters |
| `getAlertGroups` | Get alerts grouped by routing labels |
| `getCriticalAlerts` | Get critical severity alerts only |
| `getAlertingSummary` | Summary: counts by severity, top alerts, namespaces |

### Silences

| Tool | Description |
|------|-------------|
| `getSilences` | List silences by state |
| `createSilence` | Create a silence for an alert |
| `deleteSilence` | Delete a silence by ID |

### Status

| Tool | Description |
|------|-------------|
| `getAlertmanagerStatus` | Server status, version, cluster info |
| `getReceivers` | List notification receivers |

### Troubleshooting

| Tool | Description |
|------|-------------|
| `investigateAlert` | Deep investigation of a specific alert |
| `getAlertHistory` | Alert history and analysis guidance |
| `correlateAlerts` | Find correlated alerts by shared labels |

---

## Example Prompts

```
"What alerts are currently firing?"
"Are there any critical alerts?"
"Give me a summary of the alerting status"
"Investigate the HighMemoryUsage alert"
"Create a 2-hour silence for PodCrashLooping"
"What receivers are configured?"
"Find correlated alerts to identify the root cause"
"Show me alert history for KubeNodeNotReady"
```

---

## Development

### Build

```bash
make build              # Build for current platform
make build-all-platforms # Cross-compile for all platforms
```

### Container

```bash
podman build -f Containerfile -t mcp-alertmanager .
```

---

## License

[MIT](LICENSE)
