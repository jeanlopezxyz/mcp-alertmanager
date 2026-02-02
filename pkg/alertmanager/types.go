package alertmanager

import "time"

// GettableAlert represents an alert from the Alertmanager v2 API.
type GettableAlert struct {
	Annotations  map[string]string `json:"annotations"`
	EndsAt       time.Time         `json:"endsAt"`
	Fingerprint  string            `json:"fingerprint"`
	Receivers    []Receiver        `json:"receivers"`
	StartsAt     time.Time         `json:"startsAt"`
	Status       AlertStatus       `json:"status"`
	UpdatedAt    time.Time         `json:"updatedAt"`
	GeneratorURL string            `json:"generatorURL"`
	Labels       map[string]string `json:"labels"`
}

// AlertStatus represents the status of an alert.
type AlertStatus struct {
	InhibitedBy []string `json:"inhibitedBy"`
	SilencedBy  []string `json:"silencedBy"`
	State       string   `json:"state"` // active, suppressed, unprocessed
}

// AlertGroup represents a group of alerts.
type AlertGroup struct {
	Alerts   []GettableAlert   `json:"alerts"`
	Labels   map[string]string `json:"labels"`
	Receiver Receiver          `json:"receiver"`
}

// Receiver represents a notification receiver.
type Receiver struct {
	Name string `json:"name"`
}

// GettableSilence represents a silence from the Alertmanager v2 API.
type GettableSilence struct {
	ID        string        `json:"id"`
	Status    SilenceStatus `json:"status"`
	UpdatedAt time.Time     `json:"updatedAt"`
	Comment   string        `json:"comment"`
	CreatedBy string        `json:"createdBy"`
	EndsAt    time.Time     `json:"endsAt"`
	StartsAt  time.Time     `json:"startsAt"`
	Matchers  []Matcher     `json:"matchers"`
}

// SilenceStatus represents the status of a silence.
type SilenceStatus struct {
	State string `json:"state"` // active, pending, expired
}

// Matcher represents an alert matcher in a silence.
type Matcher struct {
	IsEqual bool   `json:"isEqual"`
	IsRegex bool   `json:"isRegex"`
	Name    string `json:"name"`
	Value   string `json:"value"`
}

// PostableSilence is the payload for creating a silence.
type PostableSilence struct {
	ID        string    `json:"id,omitempty"`
	Comment   string    `json:"comment"`
	CreatedBy string    `json:"createdBy"`
	EndsAt    time.Time `json:"endsAt"`
	StartsAt  time.Time `json:"startsAt"`
	Matchers  []Matcher `json:"matchers"`
}

// AlertmanagerStatus represents the Alertmanager status response.
type AlertmanagerStatus struct {
	Cluster    ClusterStatus  `json:"cluster"`
	Config     ConfigStatus   `json:"config"`
	Uptime     time.Time      `json:"uptime"`
	VersionInfo VersionInfo   `json:"versionInfo"`
}

// ClusterStatus represents the cluster status.
type ClusterStatus struct {
	Name   string   `json:"name"`
	Peers  []Peer   `json:"peers"`
	Status string   `json:"status"`
}

// Peer represents a cluster peer.
type Peer struct {
	Address string `json:"address"`
	Name    string `json:"name"`
}

// ConfigStatus holds the active configuration.
type ConfigStatus struct {
	Original string `json:"original"`
}

// VersionInfo holds version information.
type VersionInfo struct {
	Branch    string `json:"branch"`
	BuildDate string `json:"buildDate"`
	BuildUser string `json:"buildUser"`
	GoVersion string `json:"goVersion"`
	Revision  string `json:"revision"`
	Version   string `json:"version"`
}
