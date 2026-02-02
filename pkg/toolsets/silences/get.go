package silences

import (
	"context"
	"fmt"

	"github.com/google/jsonschema-go/jsonschema"
	"github.com/modelcontextprotocol/go-sdk/mcp"

	"github.com/jeanlopezxyz/mcp-alertmanager/pkg/alertmanager"
	"github.com/jeanlopezxyz/mcp-alertmanager/pkg/mcputil"
)

// Register registers all silence-related tools.
func Register(s *mcp.Server, client *alertmanager.Client) {
	registerGetSilences(s, client)
	registerCreateSilence(s, client)
	registerDeleteSilence(s, client)
}

func registerGetSilences(s *mcp.Server, client *alertmanager.Client) {
	s.AddTool(&mcp.Tool{
		Name:        "getSilences",
		Description: "List silences. Filter by state: 'active', 'pending', 'expired', or omit for all.",
		Annotations: &mcp.ToolAnnotations{
			Title:        "Silences: Get Silences",
			ReadOnlyHint: true,
		},
		InputSchema: &jsonschema.Schema{
			Type: "object",
			Properties: map[string]*jsonschema.Schema{
				"state": {
					Type:        "string",
					Description: "State: 'active', 'pending', 'expired'",
				},
			},
		},
	}, func(ctx context.Context, request *mcp.CallToolRequest) (*mcp.CallToolResult, error) {
		args, err := mcputil.GetArguments(request)
		if err != nil {
			return mcputil.NewErrorResult(err.Error()), nil
		}

		state, _ := args["state"].(string)

		result, err := client.GetSilences(state)
		if err != nil {
			return mcputil.NewErrorResult(fmt.Sprintf("Failed to get silences: %v", err)), nil
		}
		return mcputil.NewTextResult(result), nil
	})
}
