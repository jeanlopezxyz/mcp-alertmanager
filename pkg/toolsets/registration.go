package toolsets

import (
	"github.com/modelcontextprotocol/go-sdk/mcp"

	"github.com/jeanlopezxyz/mcp-alertmanager/pkg/alertmanager"
	"github.com/jeanlopezxyz/mcp-alertmanager/pkg/toolsets/alerts"
	"github.com/jeanlopezxyz/mcp-alertmanager/pkg/toolsets/silences"
	"github.com/jeanlopezxyz/mcp-alertmanager/pkg/toolsets/status"
	"github.com/jeanlopezxyz/mcp-alertmanager/pkg/toolsets/troubleshooting"
)

// RegisterAll registers all Alertmanager MCP tools with the server.
func RegisterAll(s *mcp.Server, client *alertmanager.Client) {
	alerts.Register(s, client)
	silences.Register(s, client)
	status.Register(s, client)
	troubleshooting.Register(s, client)
}
