package status

import (
	"context"
	"fmt"

	"github.com/google/jsonschema-go/jsonschema"
	"github.com/modelcontextprotocol/go-sdk/mcp"

	"github.com/jeanlopezxyz/mcp-alertmanager/pkg/alertmanager"
	"github.com/jeanlopezxyz/mcp-alertmanager/pkg/mcputil"
)

// Register registers all status-related tools.
func Register(s *mcp.Server, client *alertmanager.Client) {
	registerGetStatus(s, client)
	registerGetReceivers(s, client)
}

func registerGetStatus(s *mcp.Server, client *alertmanager.Client) {
	s.AddTool(&mcp.Tool{
		Name:        "getAlertmanagerStatus",
		Description: "Get Alertmanager server status: version, uptime, cluster info.",
		Annotations: &mcp.ToolAnnotations{
			Title:        "Status: Get Alertmanager Status",
			ReadOnlyHint: true,
		},
		InputSchema: &jsonschema.Schema{
			Type: "object",
		},
	}, func(ctx context.Context, request *mcp.CallToolRequest) (*mcp.CallToolResult, error) {
		result, err := client.GetStatus()
		if err != nil {
			return mcputil.NewErrorResult(fmt.Sprintf("Failed to get status: %v", err)), nil
		}
		return mcputil.NewTextResult(result), nil
	})
}
