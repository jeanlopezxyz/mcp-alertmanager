package alerts

import (
	"context"
	"fmt"

	"github.com/google/jsonschema-go/jsonschema"
	"github.com/modelcontextprotocol/go-sdk/mcp"

	"github.com/jeanlopezxyz/mcp-alertmanager/pkg/alertmanager"
	"github.com/jeanlopezxyz/mcp-alertmanager/pkg/mcputil"
)

// Register registers all alert-related tools.
func Register(s *mcp.Server, client *alertmanager.Client) {
	registerGetAlerts(s, client)
	registerGetAlertGroups(s, client)
	registerGetCriticalAlerts(s, client)
	registerGetAlertingSummary(s, client)
}

func registerGetAlerts(s *mcp.Server, client *alertmanager.Client) {
	s.AddTool(&mcp.Tool{
		Name:        "getAlerts",
		Description: "Get alerts from Alertmanager. Returns active alerts by default. Filter by: active, silenced, inhibited, or label (e.g., 'severity=critical').",
		Annotations: &mcp.ToolAnnotations{
			Title:        "Alerts: Get Alerts",
			ReadOnlyHint: true,
		},
		InputSchema: &jsonschema.Schema{
			Type: "object",
			Properties: map[string]*jsonschema.Schema{
				"active": {
					Type:        "string",
					Description: "Include active alerts (true/false)",
				},
				"silenced": {
					Type:        "string",
					Description: "Include silenced alerts (true/false)",
				},
				"inhibited": {
					Type:        "string",
					Description: "Include inhibited alerts (true/false)",
				},
				"filterLabel": {
					Type:        "string",
					Description: "Label filter: 'key=value'",
				},
			},
		},
	}, func(ctx context.Context, request *mcp.CallToolRequest) (*mcp.CallToolResult, error) {
		args, err := mcputil.GetArguments(request)
		if err != nil {
			return mcputil.NewErrorResult(err.Error()), nil
		}

		active, _ := args["active"].(string)
		silenced, _ := args["silenced"].(string)
		inhibited, _ := args["inhibited"].(string)
		filterLabel, _ := args["filterLabel"].(string)

		result, err := client.GetAlerts(active, silenced, inhibited, filterLabel)
		if err != nil {
			return mcputil.NewErrorResult(fmt.Sprintf("Failed to get alerts: %v", err)), nil
		}
		return mcputil.NewTextResult(result), nil
	})
}
