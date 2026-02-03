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

Automatically connects to Alertmanager running in OpenShift/Kubernetes via the K8S API service proxy. Uses native kubeconfig/in-cluster config via client-go. No `kubectl` or port-forwarding required.

Default: `openshift-monitoring/alertmanager-operated:9093`

```json
{
  "mcpServers": {
    "alertmanager": {
      "command": "npx",
      "args": ["-y", "mcp-alertmanager@latest"]
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

| Variable | Description |
|----------|-------------|
| `ALERTMANAGER_URL` | Direct Alertmanager API URL (overrides K8S auto-connect) |

### CLI Flags

| Flag | Description | Default |
|------|-------------|---------|
| `--url` | Direct Alertmanager URL | - |
| `--namespace` | Kubernetes namespace | `openshift-monitoring` |
| `--service` | Kubernetes service name | `alertmanager-operated` |
| `--service-port` | Kubernetes service port | `9093` |
| `--service-scheme` | Service scheme (http/https) | `https` |
| `--kubeconfig` | Path to kubeconfig file | auto-detect |

**Precedence:** `--url` / `ALERTMANAGER_URL` > K8S auto-connect

**Connection strategy:**
1. Direct URL (if `--url` or `ALERTMANAGER_URL` is set)
2. K8S API proxy (auto-detect kubeconfig or in-cluster config)

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

## Kubernetes / OpenShift Deployment

### Container Image

The container image is available on GitHub Container Registry:

```
ghcr.io/jeanlopezxyz/mcp-alertmanager:latest
```

### Helm Chart

Deploy using the included Helm chart:

```bash
# Add the chart repository (or use local chart)
helm upgrade --install mcp-alertmanager ./charts/mcp-alertmanager \
  --namespace mcp-servers \
  --create-namespace \
  --set openshift=true
```

#### Helm Values

| Parameter | Description | Default |
|-----------|-------------|---------|
| `image.registry` | Container registry | `ghcr.io` |
| `image.repository` | Image repository | `jeanlopezxyz/mcp-alertmanager` |
| `image.version` | Image tag | `latest` |
| `openshift` | Enable OpenShift Routes | `false` |
| `service.port` | Service port | `8080` |
| `alertmanager.namespace` | Alertmanager namespace | `openshift-monitoring` |
| `alertmanager.service` | Alertmanager service name | `alertmanager-operated` |
| `rbac.useClusterReader` | Use cluster-reader role | `true` |

#### Example with custom Alertmanager

```bash
helm upgrade --install mcp-alertmanager ./charts/mcp-alertmanager \
  --namespace mcp-servers \
  --set openshift=true \
  --set alertmanager.url=http://alertmanager.monitoring:9093
```

---

## License

[MIT](LICENSE)
