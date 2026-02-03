# Example Prompts for Alertmanager MCP Server

This document provides example prompts that demonstrate how AI assistants can use the Alertmanager MCP Server tools effectively.

## Alert Investigation

### Show critical alerts
> "Show me all critical alerts currently firing in the cluster. What services are affected?"

### Investigate a firing alert
> "Investigate the `KubePodCrashLooping` alert. Show me all instances, how long they've been firing, and what pods are affected."

### Get alerting summary
> "Give me a summary of the current alerting state. How many alerts are firing by severity? Which namespaces are most affected?"

## Alert Correlation

### Correlate related alerts
> "Are there any alerts that share common labels like namespace, pod, or node? Help me identify related issues that might have a common root cause."

### Check alert groups
> "Show me how alerts are currently grouped for notifications. Are there any large groups that suggest a widespread issue?"

## Silence Management

### Create a silence
> "Create a 2-hour silence for the `KubeDeploymentReplicasMismatch` alert. We're doing a planned deployment."

### List active silences
> "Show me all active silences. Are any about to expire?"

### Remove a silence
> "The maintenance window is over. Remove the silence we created for the deployment."

## Alert History

### Check alert history
> "What's the history of the `HighMemoryUsage` alert? Has it been firing frequently?"

### Track alert patterns
> "Has the `NodeNotReady` alert been firing intermittently? Show me recent instances."

## Status and Configuration

### Check Alertmanager status
> "What's the current status of Alertmanager? Show me the version, uptime, and cluster information."

### List notification receivers
> "What notification receivers are configured? Which ones handle critical alerts?"

## Incident Response

### Triage current incidents
> "I'm starting incident response. Give me a prioritized view of all firing alerts, starting with critical severity."

### Assess blast radius
> "The `etcd` alerts are firing. Help me understand the blast radius - what other alerts might be related?"

## Tips for Effective Prompts

- **Start with the summary** - Ask for an alerting summary first to understand the current state before diving into specifics.
- **Use alert names** - When investigating, reference the exact alert name for precise results.
- **Correlate alerts** - Always check for correlated alerts to identify root causes rather than symptoms.
- **Manage silences carefully** - Always include a reason and appropriate duration when creating silences.
