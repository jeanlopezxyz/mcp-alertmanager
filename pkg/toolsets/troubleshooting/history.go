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

func registerGetAlertHistory(s *mcp.Server, client *alertmanager.Client) {
	s.AddTool(&mcp.Tool{
		Name:        "getAlertHistory",
		Description: "Get alert history for a specific alert. Shows current/recent instances and guidance for historical analysis.",
		Annotations: &mcp.ToolAnnotations{
			Title:        "Troubleshooting: Get Alert History",
			ReadOnlyHint: true,
		},
		InputSchema: &jsonschema.Schema{
			Type: "object",
			Properties: map[string]*jsonschema.Schema{
				"alertName": {
					Type:        "string",
					Description: "Alert name to get history for",
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

		// Get all alerts (all states)
		alerts, err := client.GetAlertsRaw("true", "true", "true")
		if err != nil {
			return mcputil.NewErrorResult(fmt.Sprintf("Failed to get alerts: %v", err)), nil
		}

		var sb strings.Builder
		sb.WriteString(fmt.Sprintf("=== Alert History: %s ===\n\n", alertName))

		var matching []alertmanager.GettableAlert
		for _, a := range alerts {
			if a.Labels["alertname"] == alertName {
				matching = append(matching, a)
			}
		}

		if len(matching) == 0 {
			sb.WriteString("No current instances found.\n\n")
		} else {
			sb.WriteString(fmt.Sprintf("Current Instances: %d\n\n", len(matching)))
			for i, a := range matching {
				sb.WriteString(fmt.Sprintf("Instance %d:\n", i+1))
				sb.WriteString(fmt.Sprintf("  State: %s\n", a.Status.State))
				sb.WriteString(fmt.Sprintf("  Started: %s\n", a.StartsAt.Format(time.RFC3339)))
				sb.WriteString(fmt.Sprintf("  Duration: %s\n", time.Since(a.StartsAt).Truncate(time.Second)))
				sb.WriteString(fmt.Sprintf("  Severity: %s\n", a.Labels["severity"]))
				if ns := a.Labels["namespace"]; ns != "" {
					sb.WriteString(fmt.Sprintf("  Namespace: %s\n", ns))
				}
				sb.WriteString("\n")
			}
		}

		sb.WriteString("--- Historical Analysis Guidance ---\n")
		sb.WriteString("Alertmanager only stores current/active alerts.\n")
		sb.WriteString("For historical alert data, query Prometheus with:\n")
		sb.WriteString(fmt.Sprintf("  ALERTS{alertname=\"%s\"}\n", alertName))
		sb.WriteString(fmt.Sprintf("  ALERTS_FOR_STATE{alertname=\"%s\"}\n", alertName))

		return mcputil.NewTextResult(sb.String()), nil
	})
}
