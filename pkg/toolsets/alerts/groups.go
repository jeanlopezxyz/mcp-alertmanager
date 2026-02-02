package alerts

import (
	"context"
	"fmt"

	"github.com/google/jsonschema-go/jsonschema"
	"github.com/modelcontextprotocol/go-sdk/mcp"

	"github.com/jeanlopezxyz/mcp-alertmanager/pkg/alertmanager"
	"github.com/jeanlopezxyz/mcp-alertmanager/pkg/mcputil"
)

func registerGetAlertGroups(s *mcp.Server, client *alertmanager.Client) {
	s.AddTool(&mcp.Tool{
		Name:        "getAlertGroups",
		Description: "Get alerts grouped by routing labels. Shows how alerts are batched for notifications.",
		Annotations: &mcp.ToolAnnotations{
			Title:        "Alerts: Get Alert Groups",
			ReadOnlyHint: true,
		},
		InputSchema: &jsonschema.Schema{
			Type: "object",
		},
	}, func(ctx context.Context, request *mcp.CallToolRequest) (*mcp.CallToolResult, error) {
		result, err := client.GetAlertGroups()
		if err != nil {
			return mcputil.NewErrorResult(fmt.Sprintf("Failed to get alert groups: %v", err)), nil
		}
		return mcputil.NewTextResult(result), nil
	})
}
