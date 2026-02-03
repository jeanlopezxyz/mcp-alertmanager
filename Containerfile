FROM golang:1.25 AS builder

WORKDIR /app
COPY go.mod go.sum ./
RUN go mod download
COPY . .
RUN CGO_ENABLED=0 go build -ldflags "-s -w" -o mcp-alertmanager ./cmd/mcp-alertmanager

FROM registry.access.redhat.com/ubi9/ubi-minimal:latest

LABEL io.modelcontextprotocol.server.name="io.github.jeanlopezxyz/mcp-alertmanager"
LABEL io.k8s.display-name="MCP Alertmanager Server"
LABEL io.openshift.tags="mcp,alertmanager,monitoring,alerts"
LABEL maintainer="Jean Lopez"

WORKDIR /app
COPY --from=builder /app/mcp-alertmanager /app/mcp-alertmanager

USER 65532:65532
ENTRYPOINT ["/app/mcp-alertmanager"]
EXPOSE 8080
