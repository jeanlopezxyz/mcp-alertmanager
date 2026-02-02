package silences

import (
	"context"
	"fmt"

	"github.com/google/jsonschema-go/jsonschema"
	"github.com/modelcontextprotocol/go-sdk/mcp"
	"k8s.io/utils/ptr"

	"github.com/jeanlopezxyz/mcp-alertmanager/pkg/alertmanager"
	"github.com/jeanlopezxyz/mcp-alertmanager/pkg/mcputil"
)

func registerDeleteSilence(s *mcp.Server, client *alertmanager.Client) {
	s.AddTool(&mcp.Tool{
		Name:        "deleteSilence",
		Description: "Delete a silence by ID. Get ID from getSilences output.",
		Annotations: &mcp.ToolAnnotations{
			Title:           "Silences: Delete Silence",
			ReadOnlyHint:    false,
			DestructiveHint: ptr.To(true),
		},
		InputSchema: &jsonschema.Schema{
			Type: "object",
			Properties: map[string]*jsonschema.Schema{
				"silenceId": {
					Type:        "string",
					Description: "Silence UUID",
				},
			},
			Required: []string{"silenceId"},
		},
	}, func(ctx context.Context, request *mcp.CallToolRequest) (*mcp.CallToolResult, error) {
		args, err := mcputil.GetArguments(request)
		if err != nil {
			return mcputil.NewErrorResult(err.Error()), nil
		}

		silenceID, _ := args["silenceId"].(string)
		if silenceID == "" {
			return mcputil.NewErrorResult("silenceId parameter is required"), nil
		}

		if err := client.DeleteSilence(silenceID); err != nil {
			return mcputil.NewErrorResult(fmt.Sprintf("Failed to delete silence: %v", err)), nil
		}
		return mcputil.NewTextResult(fmt.Sprintf("Silence %s deleted successfully", silenceID)), nil
	})
}
