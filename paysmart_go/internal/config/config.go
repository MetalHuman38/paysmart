package config

import (
	"log"
	"os"
	"regexp"
	"strings"
)

type Config struct {
	AllowedEmailDomains map[string]struct{}
	BlockedEmails       map[string]struct{}
	AllowedProviders    map[string]struct{}
	AllowedTenants      map[string]struct{}
	AuthGate            bool
	BaseURL             string
	DisposablePatterns  []*regexp.Regexp
	BlockedPhones       map[string]struct{}
	ProjectID           string
}

func mustEnvCSVSet(name string, lower bool) map[string]struct{} {
	raw := strings.TrimSpace(os.Getenv(name))
	out := make(map[string]struct{})
	if raw == "" {
		return out
	}
	for _, p := range strings.Split(raw, ",") {
		s := strings.TrimSpace(p)
		if s == "" {
			continue
		}
		if lower {
			s = strings.ToLower(s)
		}
		out[s] = struct{}{}
	}
	return out
}

func compileRegexList(rawCSV string) []*regexp.Regexp {
	var out []*regexp.Regexp
	raw := strings.TrimSpace(rawCSV)
	if raw == "" {
		return out
	}
	for _, p := range strings.Split(raw, ",") {
		ps := strings.TrimSpace(p)
		if ps == "" {
			continue
		}
		rx, err := regexp.Compile("(?i)" + ps)
		if err != nil {
			log.Printf("[authgate] invalid regex skipped: %q (%v)", ps, err)
			continue
		}
		out = append(out, rx)
	}
	return out
}

func Load() *Config {
	return &Config{
		AllowedEmailDomains: mustEnvCSVSet("ALLOWED_EMAIL_DOMAINS", true),
		BlockedEmails:       mustEnvCSVSet("BLOCKED_EMAILS", true),
		AllowedProviders:    mustEnvCSVSet("ALLOWED_PROVIDERS", true),
		AllowedTenants:      mustEnvCSVSet("ALLOWED_TENANTS", false),
		AuthGate:            true,
		DisposablePatterns:  compileRegexList(os.Getenv("DISPOSABLE_PATTERNS")),
		BlockedPhones:       mustEnvCSVSet("BLOCKED_PHONES", false),
		BaseURL:             os.Getenv("BASE_URL"),
		ProjectID:           os.Getenv("GCP_PROJECT"),
	}
}
