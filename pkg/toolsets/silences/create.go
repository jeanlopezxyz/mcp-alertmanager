package silences

import (
	"context"
	"fmt"
	"time"

	"github.com/google/jsonschema-go/jsonschema"
	"github.com/modelcontextprotocol/go-sdk/mcp"
	"k8s.io/utils/ptr"

	"github.com/jeanlopezxyz/mcp-alertmanager/pkg/alertmanager"
	"github.com/jeanlopezxyz/mcp-alertmanager/pkg/mcputil"
)

func registerCreateSilence(s *mcp.Server, client *alertmanager.Client) {
	s.AddTool(&mcp.Tool{
		Name:        "createSilence",
		Description: "Create a silence for an alert. Duration format: '30m', '2h', '1d'. Max 30 days.",
		Annotations: &mcp.ToolAnnotations{
			Title:           "Silences: Create Silence",
			ReadOnlyHint:    false,
			DestructiveHint: ptr.To(false),
		},
		InputSchema: &jsonschema.Schema{
			Type: "object",
			Properties: map[string]*jsonschema.Schema{
				"alertName": {
					Type:        "string",
					Description: "Alert name to silence",
				},
				"duration": {
					Type:        "string",
					Description: "Duration: '30m', '2h', '1d' (default: 2h)",
				},
				"comment": {
					Type:        "string",
					Description: "Reason for silence (default: 'Silenced via MCP')",
				},
				"createdBy": {
					Type:        "string",
					Description: "Creator name (default: 'mcp-alertmanager')",
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

		duration := "2h"
		if d, ok := args["duration"].(string); ok && d != "" {
			duration = d
		}

		comment := "Silenced via MCP"
		if c, ok := args["comment"].(string); ok && c != "" {
			comment = c
		}

		createdBy := "mcp-alertmanager"
		if cb, ok := args["createdBy"].(string); ok && cb != "" {
			createdBy = cb
		}

		dur, err := parseDuration(duration)
		if err != nil {
			return mcputil.NewErrorResult(fmt.Sprintf("Invalid duration: %v", err)), nil
		}

		// Max 30 days
		if dur > 30*24*time.Hour {
			return mcputil.NewErrorResult("Duration cannot exceed 30 days"), nil
		}

		now := time.Now()
		silence := alertmanager.PostableSilence{
			Comment:   comment,
			CreatedBy: createdBy,
			StartsAt:  now,
			EndsAt:    now.Add(dur),
			Matchers: []alertmanager.Matcher{
				{
					IsEqual: true,
					IsRegex: false,
					Name:    "alertname",
					Value:   alertName,
				},
			},
		}

		result, err := client.CreateSilence(silence)
		if err != nil {
			return mcputil.NewErrorResult(fmt.Sprintf("Failed to create silence: %v", err)), nil
		}
		return mcputil.NewTextResult(fmt.Sprintf("Silence created successfully:\n%s", result)), nil
	})
}

func parseDuration(s string) (time.Duration, error) {
	if len(s) < 2 {
		return 0, fmt.Errorf("invalid duration: %s", s)
	}
	unit := s[len(s)-1]
	valStr := s[:len(s)-1]
	var val int
	if _, err := fmt.Sscanf(valStr, "%d", &val); err != nil {
		return 0, fmt.Errorf("invalid duration value: %s", s)
	}
	switch unit {
	case 'm':
		return time.Duration(val) * time.Minute, nil
	case 'h':
		return time.Duration(val) * time.Hour, nil
	case 'd':
		return time.Duration(val) * 24 * time.Hour, nil
	default:
		return 0, fmt.Errorf("unknown duration unit: %c", unit)
	}
}
