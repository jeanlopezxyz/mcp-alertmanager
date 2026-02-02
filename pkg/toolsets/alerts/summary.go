package alerts

import (
	"context"
	"fmt"
	"sort"
	"strings"

	"github.com/google/jsonschema-go/jsonschema"
	"github.com/modelcontextprotocol/go-sdk/mcp"

	"github.com/jeanlopezxyz/mcp-alertmanager/pkg/alertmanager"
	"github.com/jeanlopezxyz/mcp-alertmanager/pkg/mcputil"
)

func registerGetAlertingSummary(s *mcp.Server, client *alertmanager.Client) {
	s.AddTool(&mcp.Tool{
		Name:        "getAlertingSummary",
		Description: "Get alerting summary: counts by severity, top alerts, affected namespaces.",
		Annotations: &mcp.ToolAnnotations{
			Title:        "Alerts: Get Alerting Summary",
			ReadOnlyHint: true,
		},
		InputSchema: &jsonschema.Schema{
			Type: "object",
		},
	}, func(ctx context.Context, request *mcp.CallToolRequest) (*mcp.CallToolResult, error) {
		alerts, err := client.GetAlertsRaw("true", "", "")
		if err != nil {
			return mcputil.NewErrorResult(fmt.Sprintf("Failed to get alerts: %v", err)), nil
		}

		// Count by severity
		severityCounts := make(map[string]int)
		alertCounts := make(map[string]int)
		namespaceCounts := make(map[string]int)

		for _, alert := range alerts {
			severity := alert.Labels["severity"]
			if severity == "" {
				severity = "unknown"
			}
			severityCounts[severity]++

			name := alert.Labels["alertname"]
			alertCounts[name]++

			ns := alert.Labels["namespace"]
			if ns != "" {
				namespaceCounts[ns]++
			}
		}

		var sb strings.Builder
		sb.WriteString(fmt.Sprintf("=== Alerting Summary ===\nTotal Active Alerts: %d\n\n", len(alerts)))

		sb.WriteString("--- By Severity ---\n")
		for sev, count := range severityCounts {
			sb.WriteString(fmt.Sprintf("  %s: %d\n", sev, count))
		}

		sb.WriteString("\n--- Top Alerts ---\n")
		type kv struct {
			Key   string
			Value int
		}
		var sortedAlerts []kv
		for k, v := range alertCounts {
			sortedAlerts = append(sortedAlerts, kv{k, v})
		}
		sort.Slice(sortedAlerts, func(i, j int) bool {
			return sortedAlerts[i].Value > sortedAlerts[j].Value
		})
		for i, kv := range sortedAlerts {
			if i >= 10 {
				break
			}
			sb.WriteString(fmt.Sprintf("  %s: %d instances\n", kv.Key, kv.Value))
		}

		sb.WriteString("\n--- Affected Namespaces ---\n")
		var sortedNs []kv
		for k, v := range namespaceCounts {
			sortedNs = append(sortedNs, kv{k, v})
		}
		sort.Slice(sortedNs, func(i, j int) bool {
			return sortedNs[i].Value > sortedNs[j].Value
		})
		for _, kv := range sortedNs {
			sb.WriteString(fmt.Sprintf("  %s: %d alerts\n", kv.Key, kv.Value))
		}

		return mcputil.NewTextResult(sb.String()), nil
	})
}
