package handlers

import (
	"context"
	"fmt"
	"net/http"
	"paysmart_go/internal/bootstrap"
	"strings"

	authsvc "paysmart_go/internal/auth"
	_ "paysmart_go/internal/bootstrap"
	"paysmart_go/internal/httpx"

	"cloud.google.com/go/firestore"
	"firebase.google.com/go/v4/auth"
	"github.com/go-chi/chi/v5"
)

func verifyBearer(ctx context.Context, au *auth.Client, h http.Header) (*auth.Token, error) {
	authz := h.Get("Authorization")
	if authz == "" || !strings.HasPrefix(strings.ToLower(authz), "bearer ") {
		return nil, fmt.Errorf("missing bearer token")
	}
	idToken := strings.TrimSpace(authz[len("Bearer "):])
	return au.VerifyIDToken(ctx, idToken)
}

// usersEnsure POST /users/ensure
func usersEnsure(deps *authsvc.Deps) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		if r.Method == http.MethodOptions {
			httpx.CORS(w)
			w.WriteHeader(http.StatusNoContent)
			return
		}
		ctx := r.Context()

		// LAZY INIT: resolve deps if nil
		if deps == nil || deps.Auth == nil || deps.Firestore == nil {
			var err error
			deps, err = bootstrap.Deps(ctx)
			if err != nil || deps == nil || deps.Auth == nil || deps.Firestore == nil {
				httpx.Deny(w, 500, "internal", "bootstrap failed")
				return
			}
		}

		tok, err := verifyBearer(ctx, deps.Auth, r.Header)
		if err != nil {
			httpx.Deny(w, 401, "unauthenticated", "Invalid ID token")
			return
		}

		email, _ := tok.Claims["email"].(string)
		verified, _ := tok.Claims["email_verified"].(bool)
		if !verified {
			httpx.Deny(w, 412, "failed-precondition", "Email not verified")
			return
		}

		uid := tok.UID
		uRec, err := deps.Auth.GetUser(ctx, uid)
		if err != nil {
			httpx.Deny(w, 500, "internal", "Failed to load user")
			return
		}

		var providerIds []string
		for _, p := range uRec.ProviderUserInfo {
			if p.ProviderID != "" {
				providerIds = append(providerIds, p.ProviderID)
			}
		}
		primary := "password"
		if len(providerIds) > 0 {
			primary = providerIds[0]
		}

		doc := deps.Firestore.Collection("users").Doc(uid)
		now := firestore.ServerTimestamp

		// Base data to merge on every sign-in
		data := map[string]any{
			"authProvider":  normalizeProvider(primary),
			"email":         email,
			"emailVerified": true,
			"isAnonymous":   len(uRec.ProviderUserInfo) == 0,
			"status":        "verified",
			"providerIds":   providerIds,
			"lastSignedIn":  now,
		}
		if uRec.DisplayName != "" {
			data["displayName"] = uRec.DisplayName
		}
		if uRec.PhotoURL != "" && (strings.HasPrefix(uRec.PhotoURL, "https://") || strings.HasPrefix(uRec.PhotoURL, "https://")) {
			data["photoURL"] = uRec.PhotoURL
		}
		if uRec.PhoneNumber != "" {
			data["phoneNumber"] = uRec.PhoneNumber
		}
		if uRec.TenantID != "" {
			data["tenantId"] = uRec.TenantID
		}

		// Transaction: add createdAt only if the doc does not exist yet
		err = deps.Firestore.RunTransaction(ctx, func(ctx context.Context, tx *firestore.Transaction) error {
			snap, err := tx.Get(doc)
			if err != nil {
				return err
			}

			// copy base data so we can add createdAt conditionally
			write := make(map[string]any, len(data)+2)
			for k, v := range data {
				write[k] = v
			}
			if !snap.Exists() {
				write["createdAt"] = now
			}
			return tx.Set(doc, write, firestore.MergeAll)
		})
		if err != nil {
			httpx.Deny(w, 500, "internal", "Firestore write failed: "+err.Error())
			return
		}

		httpx.JSON(w, 200, map[string]string{"status": "ok"})
	}
}

// Mount Wire routes (optional helper)
func Mount(r chi.Router, deps *authsvc.Deps) {
	r.Post("/auth/beforeCreate", beforeCreate(deps))
	r.Post("/auth/beforeSignIn", beforeSignIn())
	r.Post("/users/ensure", usersEnsure(deps))
}
