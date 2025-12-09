package handlers

import (
	"strings"

	"paysmart_go/internal/config"
)

func detectProvider(eventType string, data map[string]any, user map[string]any) string {
	if cred, _ := data["credential"].(map[string]any); cred != nil {
		if sm, _ := cred["signInMethod"].(string); sm != "" {
			return strings.ToLower(sm)
		}
	}
	if p, _ := user["signInProvider"].(string); p != "" {
		return strings.ToLower(p)
	}
	if p, _ := user["providerId"].(string); p != "" {
		return strings.ToLower(p)
	}
	if i := strings.LastIndex(eventType, ":"); i != -1 {
		return strings.ToLower(eventType[i+1:])
	}
	return ""
}

func normalizeProvider(p string) string {
	switch p {
	case "google.com":
		return "google"
	case "facebook.com":
		return "facebook"
	case "apple.com":
		return "apple"
	case "password", "emaillink", "email":
		return "password"
	default:
		if p == "" {
			return "password"
		}
		return p
	}
}

func splitEmail(e string) (email string, domain string) {
	e = strings.ToLower(strings.TrimSpace(e))
	if e == "" || !strings.Contains(e, "@") {
		return "", ""
	}
	parts := strings.SplitN(e, "@", 2)
	return e, parts[1]
}

func isDisposable(cfg *config.Config, email string) bool {
	for _, rx := range cfg.DisposablePatterns {
		if rx.MatchString(email) {
			return true
		}
	}
	return false
}
