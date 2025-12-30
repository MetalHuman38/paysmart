package handlers

import (
	"fmt"
	"paysmart_go/internal/auth"
	"strings"
)

type httpError struct {
	Code    int
	Type    string
	Message string
}

func validateCredentials(deps *auth.Deps, provider string, user map[string]any) (map[string]any, *httpError) {
	switch provider {
	case "phone":
		raw, _ := user["phoneNumber"].(string)
		phone := strings.TrimSpace(raw)
		if phone == "" {
			return nil, &httpError{403, "permission-denied", "Phone number is required."}
		}
		if _, blocked := deps.Cfg.BlockedPhones[phone]; blocked {
			return nil, &httpError{403, "permission-denied", "Phone number is blocked."}
		}

	case "email", "password", "emaillink":
		rawEmail, _ := user["email"].(string)
		email, domain := splitEmail(rawEmail)
		if email == "" {
			return nil, &httpError{400, "invalid-argument", "Email is required."}
		}
		if _, blocked := deps.Cfg.BlockedEmails[email]; blocked {
			return nil, &httpError{403, "permission-denied", "Email is blocked."}
		}
		if isDisposable(deps.Cfg, email) {
			return nil, &httpError{422, "invalid-argument", "Disposable email addresses are not allowed."}
		}
		if len(deps.Cfg.AllowedEmailDomains) > 0 {
			if _, ok := deps.Cfg.AllowedEmailDomains[domain]; !ok {
				return nil, &httpError{403, "permission-denied", fmt.Sprintf("Domain '%s' is not allowed.", domain)}
			}
		}
	}

	return map[string]any{
		"customClaims": map[string]any{"role": "user"},
	}, nil
}
