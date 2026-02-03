# Alertmanager MCP Server Evaluations

This directory contains evaluation configurations for testing the Alertmanager MCP server using the [mcpchecker](https://github.com/mcpchecker/mcpchecker) framework.

## Structure

```
evals/
├── README.md                    # This file
├── mcp-config.yaml              # MCP server configuration
├── tasks/                       # Test task definitions
│   └── alertmanager/            # Alertmanager-specific tasks
│       └── get-alerts.yaml      # Example task
└── openai-agent/                # OpenAI-compatible agent configuration
    ├── agent.yaml
    └── eval.yaml
```

## Prerequisites

- Alertmanager instance accessible
- Alertmanager MCP server running at `http://localhost:8080/mcp`
- mcpchecker installed

## Running Evaluations

### Manual Run

```bash
# Set your model credentials
export MODEL_BASE_URL='https://your-api-endpoint.com/v1'
export MODEL_KEY='your-api-key'
export JUDGE_BASE_URL='https://your-judge-endpoint.com/v1'
export JUDGE_API_KEY='your-judge-api-key'
export JUDGE_MODEL_NAME='gpt-4'

# Run evaluation
mcpchecker eval evals/openai-agent/eval.yaml
```

### GitHub Actions

The `mcpchecker.yaml` workflow runs evaluations:
- Weekly on Monday at 9 AM UTC
- On demand via workflow_dispatch
- On PR comments with `/run-mcpchecker`

## Adding New Tasks

1. Create a new YAML file in `tasks/alertmanager/`
2. Define the task with prompt and expected assertions
3. Run locally to verify before committing

Example task structure:
```yaml
kind: Task
metadata:
  name: "get-critical-alerts"
prompt: "Get all critical severity alerts currently firing"
assertions:
  toolsUsed:
    - server: alertmanager
      toolPattern: "(getAlerts|getCriticalAlerts)"
```
