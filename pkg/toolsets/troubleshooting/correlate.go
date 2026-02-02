package troubleshooting

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

func registerCorrelateAlerts(s *mcp.Server, client *alertmanager.Client) {
	s.AddTool(&mcp.Tool{
		Name:        "correlateAlerts",
		Description: "Find correlated alerts that share common labels (namespace, pod, node). Helps identify related issues during incidents.",
		Annotations: &mcp.ToolAnnotations{
			Title:        "Troubleshooting: Correlate Alerts",
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

		if len(alerts) == 0 {
			return mcputil.NewTextResult("No active alerts to correlate."), nil
		}

		// Correlation labels to check
		correlationLabels := []string{"namespace", "pod", "node", "service", "job", "instance"}

		// Group alerts by correlation labels
		groups := make(map[string][]alertmanager.GettableAlert)
		for _, alert := range alerts {
			for _, label := range correlationLabels {
				if val, exists := alert.Labels[label]; exists && val != "" {
					key := fmt.Sprintf("%s=%s", label, val)
					groups[key] = append(groups[key], alert)
				}
			}
		}

		var sb strings.Builder
		sb.WriteString(fmt.Sprintf("=== Alert Correlation ===\nTotal Active Alerts: %d\n\n", len(alerts)))

		// Sort groups by number of alerts (most correlated first)
		type groupEntry struct {
			key    string
			alerts []alertmanager.GettableAlert
		}
		var sortedGroups []groupEntry
		for k, v := range groups {
			if len(v) > 1 { // Only show groups with 2+ alerts
				sortedGroups = append(sortedGroups, groupEntry{k, v})
			}
		}
		sort.Slice(sortedGroups, func(i, j int) bool {
			return len(sortedGroups[i].alerts) > len(sortedGroups[j].alerts)
		})

		if len(sortedGroups) == 0 {
			sb.WriteString("No correlated alerts found (no shared labels between alerts).\n")
		} else {
			for _, g := range sortedGroups {
				sb.WriteString(fmt.Sprintf("--- %s (%d alerts) ---\n", g.key, len(g.alerts)))
				for _, a := range g.alerts {
					severity := a.Labels["severity"]
					sb.WriteString(fmt.Sprintf("  - %s [%s] (%s)\n",
						a.Labels["alertname"], severity, a.Status.State))
				}
				sb.WriteString("\n")
			}
		}

		return mcputil.NewTextResult(sb.String()), nil
	})
}
