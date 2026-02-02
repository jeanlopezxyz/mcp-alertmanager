FROM golang:1.24 AS builder

WORKDIR /app
COPY go.mod go.sum ./
RUN go mod download
COPY . .
RUN CGO_ENABLED=0 go build -ldflags "-s -w" -o mcp-alertmanager ./cmd/mcp-alertmanager

FROM scratch
COPY --from=builder /etc/ssl/certs/ca-certificates.crt /etc/ssl/certs/
COPY --from=builder /app/mcp-alertmanager /mcp-alertmanager
USER 65532:65532
ENTRYPOINT ["/mcp-alertmanager"]
