package alerts

import (
	"context"
	"fmt"

	"github.com/google/jsonschema-go/jsonschema"
	"github.com/modelcontextprotocol/go-sdk/mcp"

	"github.com/jeanlopezxyz/mcp-alertmanager/pkg/alertmanager"
	"github.com/jeanlopezxyz/mcp-alertmanager/pkg/mcputil"
)

func registerGetCriticalAlerts(s *mcp.Server, client *alertmanager.Client) {
	s.AddTool(&mcp.Tool{
		Name:        "getCriticalAlerts",
		Description: "Get critical severity alerts only. Prioritized for incident response.",
		Annotations: &mcp.ToolAnnotations{
			Title:        "Alerts: Get Critical Alerts",
			ReadOnlyHint: true,
		},
		InputSchema: &jsonschema.Schema{
			Type: "object",
		},
	}, func(ctx context.Context, request *mcp.CallToolRequest) (*mcp.CallToolResult, error) {
		result, err := client.GetAlerts("true", "", "", `severity="critical"`)
		if err != nil {
			return mcputil.NewErrorResult(fmt.Sprintf("Failed to get critical alerts: %v", err)), nil
		}
		return mcputil.NewTextResult(result), nil
	})
}
