package status

import (
	"context"
	"fmt"

	"github.com/google/jsonschema-go/jsonschema"
	"github.com/modelcontextprotocol/go-sdk/mcp"

	"github.com/jeanlopezxyz/mcp-alertmanager/pkg/alertmanager"
	"github.com/jeanlopezxyz/mcp-alertmanager/pkg/mcputil"
)

func registerGetReceivers(s *mcp.Server, client *alertmanager.Client) {
	s.AddTool(&mcp.Tool{
		Name:        "getReceivers",
		Description: "List configured notification receivers (Slack, email, PagerDuty, etc.).",
		Annotations: &mcp.ToolAnnotations{
			Title:        "Status: Get Receivers",
			ReadOnlyHint: true,
		},
		InputSchema: &jsonschema.Schema{
			Type: "object",
		},
	}, func(ctx context.Context, request *mcp.CallToolRequest) (*mcp.CallToolResult, error) {
		result, err := client.GetReceivers()
		if err != nil {
			return mcputil.NewErrorResult(fmt.Sprintf("Failed to get receivers: %v", err)), nil
		}
		return mcputil.NewTextResult(result), nil
	})
}
