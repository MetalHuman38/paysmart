package handlers

import (
	"encoding/json"
	"fmt"
	"net/http"
	"paysmart_go/internal/bootstrap"
	"time"

	"paysmart_go/internal/auth"
	"paysmart_go/internal/httpx"
)

// beforeCreate POST /auth/beforeCreate
func beforeCreate(deps *auth.Deps) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		if r.Method == http.MethodOptions {
			httpx.CORS(w)
			w.WriteHeader(http.StatusNoContent)
			return
		}
		start := time.Now()

		// LAZY INIT: if deps were passed as nil from Mount(), resolve once here
		if deps == nil || deps.Cfg == nil {
			var err error
			deps, err = bootstrap.Deps(r.Context())
			if err != nil {
				httpx.Deny(w, 500, "internal", "bootstrap failed: "+err.Error())
				return
			}
			if deps == nil || deps.Cfg == nil {
				httpx.Deny(w, 500, "internal", "missing configuration")
				return
			}
		}

		var body beforeBody
		if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
			httpx.Deny(w, 400, "invalid-argument", "Bad JSON")
			return
		}
		user := getUserMap(beforeBody{})
		provider := detectProvider(body.EventType, body.Data, user)

		if len(user) == 0 || provider == "anonymous" || provider == "phone" || provider == "email" {
			httpx.JSON(w, 200, map[string]any{
				"userRecord": map[string]any{"customClaims": map[string]any{"role": "guest"}},
			})
			return
		}

		if len(deps.Cfg.AllowedTenants) > 0 {
			if _, ok := deps.Cfg.AllowedTenants[body.Resource]; !ok {
				httpx.Deny(w, 403, "permission-denied", "Tenant not allowed.")
				return
			}
		}
		if len(deps.Cfg.AllowedProviders) > 0 && provider != "" {
			if _, ok := deps.Cfg.AllowedProviders[provider]; !ok {
				httpx.Deny(w, 403, "permission-denied", fmt.Sprintf("Provider '%s' not allowed for sign up.", provider))
				return
			}
		}
		if provider == "" {
			httpx.Deny(w, 400, "invalid-argument", "Could not determine identity provider.")
			return
		}

		// Provider checks.
		updates, err := validateCredentials(deps, provider, user)
		if err != nil {
			httpx.Deny(w, err.Code, err.Type, err.Message)
			return
		}
		updates["displayName"] = user["displayName"]

		if time.Since(start) > 6500*time.Millisecond {
			httpx.Deny(w, 503, "deadline-exceeded", "Sign up check took too long.")
			return
		}
		httpx.JSON(w, 200, map[string]any{"userRecord": updates})
	}
}

// beforeSignIn POST /auth/beforeSignIn
func beforeSignIn() http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		if r.Method == http.MethodOptions {
			httpx.CORS(w)
			w.WriteHeader(http.StatusNoContent)
			return
		}
		var body beforeBody
		if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
			httpx.Deny(w, 400, "invalid-argument", "Bad JSON")
			return
		}
		user := getUserMap(beforeBody{})
		provider := detectProvider(body.EventType, body.Data, user)

		if len(user) == 0 || provider == "phone" || provider == "anonymous" || provider == "emaillink" || provider == "email" {
			httpx.JSON(w, 200, map[string]any{"sessionClaims": map[string]any{"ts": time.Now().Unix()}})
			return
		}
		if provider == "password" {
			if v, _ := user["emailVerified"].(bool); !v {
				httpx.Deny(w, 412, "failed-precondition", "Please verify your email before signing in.")
				return
			}
		}
		httpx.JSON(w, 200, map[string]any{"sessionClaims": map[string]any{"ts": time.Now().Unix()}})
	}
}
