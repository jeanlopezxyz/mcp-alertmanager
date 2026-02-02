package troubleshooting

import (
	"context"
	"fmt"
	"strings"
	"time"

	"github.com/google/jsonschema-go/jsonschema"
	"github.com/modelcontextprotocol/go-sdk/mcp"

	"github.com/jeanlopezxyz/mcp-alertmanager/pkg/alertmanager"
	"github.com/jeanlopezxyz/mcp-alertmanager/pkg/mcputil"
)

// Register registers all troubleshooting tools.
func Register(s *mcp.Server, client *alertmanager.Client) {
	registerInvestigateAlert(s, client)
	registerGetAlertHistory(s, client)
	registerCorrelateAlerts(s, client)
}

func registerInvestigateAlert(s *mcp.Server, client *alertmanager.Client) {
	s.AddTool(&mcp.Tool{
		Name:        "investigateAlert",
		Description: "Investigate an alert: all instances, duration, labels, silences, recommendations.",
		Annotations: &mcp.ToolAnnotations{
			Title:        "Troubleshooting: Investigate Alert",
			ReadOnlyHint: true,
		},
		InputSchema: &jsonschema.Schema{
			Type: "object",
			Properties: map[string]*jsonschema.Schema{
				"alertName": {
					Type:        "string",
					Description: "Alert name to investigate",
				},
			},
			Required: []string{"alertName"},
		},
	}, func(ctx context.Context, request *mcp.CallToolRequest) (*mcp.CallToolResult, error) {
		args, err := mcputil.GetArguments(request)
		if err != nil {
			return mcputil.NewErrorResult(err.Error()), nil
		}

		alertName, _ := args["alertName"].(string)
		if alertName == "" {
			return mcputil.NewErrorResult("alertName parameter is required"), nil
		}

		// Get all alerts (active + silenced + inhibited) for this alert name
		alerts, err := client.GetAlertsRaw("true", "true", "true")
		if err != nil {
			return mcputil.NewErrorResult(fmt.Sprintf("Failed to get alerts: %v", err)), nil
		}

		var sb strings.Builder
		sb.WriteString(fmt.Sprintf("=== Investigation: %s ===\n\n", alertName))

		var matchingAlerts []alertmanager.GettableAlert
		for _, a := range alerts {
			if a.Labels["alertname"] == alertName {
				matchingAlerts = append(matchingAlerts, a)
			}
		}

		if len(matchingAlerts) == 0 {
			sb.WriteString("No instances found for this alert.\n")
			return mcputil.NewTextResult(sb.String()), nil
		}

		sb.WriteString(fmt.Sprintf("Active Instances: %d\n\n", len(matchingAlerts)))

		for i, a := range matchingAlerts {
			sb.WriteString(fmt.Sprintf("--- Instance %d ---\n", i+1))
			sb.WriteString(fmt.Sprintf("  State: %s\n", a.Status.State))
			sb.WriteString(fmt.Sprintf("  Started: %s\n", a.StartsAt.Format(time.RFC3339)))
			duration := time.Since(a.StartsAt).Truncate(time.Second)
			sb.WriteString(fmt.Sprintf("  Duration: %s\n", duration))

			sb.WriteString("  Labels:\n")
			for k, v := range a.Labels {
				sb.WriteString(fmt.Sprintf("    %s: %s\n", k, v))
			}
			if len(a.Annotations) > 0 {
				sb.WriteString("  Annotations:\n")
				for k, v := range a.Annotations {
					sb.WriteString(fmt.Sprintf("    %s: %s\n", k, v))
				}
			}
			if len(a.Status.SilencedBy) > 0 {
				sb.WriteString(fmt.Sprintf("  Silenced by: %s\n", strings.Join(a.Status.SilencedBy, ", ")))
			}
			if len(a.Status.InhibitedBy) > 0 {
				sb.WriteString(fmt.Sprintf("  Inhibited by: %s\n", strings.Join(a.Status.InhibitedBy, ", ")))
			}
			sb.WriteString("\n")
		}

		return mcputil.NewTextResult(sb.String()), nil
	})
}
