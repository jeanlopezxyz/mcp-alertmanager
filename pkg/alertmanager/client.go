package alertmanager

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"time"
)

// Client is an HTTP client for the Alertmanager v2 API.
type Client struct {
	BaseURL    string
	HTTPClient *http.Client
}

// NewClient creates a new Alertmanager API client.
func NewClient(baseURL string, httpClient *http.Client) *Client {
	if httpClient == nil {
		httpClient = &http.Client{Timeout: 30 * time.Second}
	}
	return &Client{BaseURL: baseURL, HTTPClient: httpClient}
}

func (c *Client) doGet(path string) ([]byte, error) {
	resp, err := c.HTTPClient.Get(c.BaseURL + path)
	if err != nil {
		return nil, fmt.Errorf("request failed: %w", err)
	}
	defer resp.Body.Close()
	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("reading response: %w", err)
	}
	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("API returned status %d: %s", resp.StatusCode, string(body))
	}
	return body, nil
}

func (c *Client) doPost(path string, payload interface{}) ([]byte, error) {
	data, err := json.Marshal(payload)
	if err != nil {
		return nil, fmt.Errorf("marshaling request: %w", err)
	}
	resp, err := c.HTTPClient.Post(c.BaseURL+path, "application/json", bytes.NewReader(data))
	if err != nil {
		return nil, fmt.Errorf("request failed: %w", err)
	}
	defer resp.Body.Close()
	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("reading response: %w", err)
	}
	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("API returned status %d: %s", resp.StatusCode, string(body))
	}
	return body, nil
}

func (c *Client) doDelete(path string) error {
	req, err := http.NewRequest(http.MethodDelete, c.BaseURL+path, nil)
	if err != nil {
		return fmt.Errorf("creating request: %w", err)
	}
	resp, err := c.HTTPClient.Do(req)
	if err != nil {
		return fmt.Errorf("request failed: %w", err)
	}
	defer resp.Body.Close()
	if resp.StatusCode != http.StatusOK {
		body, _ := io.ReadAll(resp.Body)
		return fmt.Errorf("API returned status %d: %s", resp.StatusCode, string(body))
	}
	return nil
}

// GetAlerts returns alerts from Alertmanager.
func (c *Client) GetAlerts(active, silenced, inhibited, filterLabel string) (string, error) {
	params := url.Values{}
	if active != "" {
		params.Set("active", active)
	}
	if silenced != "" {
		params.Set("silenced", silenced)
	}
	if inhibited != "" {
		params.Set("inhibited", inhibited)
	}
	if filterLabel != "" {
		params.Set("filter", filterLabel)
	}
	path := "/api/v2/alerts"
	if len(params) > 0 {
		path += "?" + params.Encode()
	}
	body, err := c.doGet(path)
	if err != nil {
		return "", err
	}
	return formatJSON(body)
}

// GetAlertGroups returns alerts grouped by routing labels.
func (c *Client) GetAlertGroups() (string, error) {
	body, err := c.doGet("/api/v2/alerts/groups")
	if err != nil {
		return "", err
	}
	return formatJSON(body)
}

// GetSilences returns silences from Alertmanager.
func (c *Client) GetSilences(state string) (string, error) {
	path := "/api/v2/silences"
	if state != "" {
		path += "?state=" + url.QueryEscape(state)
	}
	body, err := c.doGet(path)
	if err != nil {
		return "", err
	}
	return formatJSON(body)
}

// CreateSilence creates a new silence.
func (c *Client) CreateSilence(silence PostableSilence) (string, error) {
	body, err := c.doPost("/api/v2/silences", silence)
	if err != nil {
		return "", err
	}
	return formatJSON(body)
}

// DeleteSilence deletes a silence by ID.
func (c *Client) DeleteSilence(silenceID string) error {
	return c.doDelete("/api/v2/silence/" + url.PathEscape(silenceID))
}

// GetStatus returns Alertmanager server status.
func (c *Client) GetStatus() (string, error) {
	body, err := c.doGet("/api/v2/status")
	if err != nil {
		return "", err
	}
	return formatJSON(body)
}

// GetReceivers returns configured notification receivers.
func (c *Client) GetReceivers() (string, error) {
	body, err := c.doGet("/api/v2/receivers")
	if err != nil {
		return "", err
	}
	return formatJSON(body)
}

// GetAlertsRaw returns raw alert data for processing.
func (c *Client) GetAlertsRaw(active, silenced, inhibited string) ([]GettableAlert, error) {
	params := url.Values{}
	if active != "" {
		params.Set("active", active)
	}
	if silenced != "" {
		params.Set("silenced", silenced)
	}
	if inhibited != "" {
		params.Set("inhibited", inhibited)
	}
	path := "/api/v2/alerts"
	if len(params) > 0 {
		path += "?" + params.Encode()
	}
	body, err := c.doGet(path)
	if err != nil {
		return nil, err
	}
	var alerts []GettableAlert
	if err := json.Unmarshal(body, &alerts); err != nil {
		return nil, fmt.Errorf("parsing alerts: %w", err)
	}
	return alerts, nil
}

func formatJSON(data []byte) (string, error) {
	var obj interface{}
	if err := json.Unmarshal(data, &obj); err != nil {
		return string(data), nil
	}
	formatted, err := json.MarshalIndent(obj, "", "  ")
	if err != nil {
		return string(data), nil
	}
	return string(formatted), nil
}
