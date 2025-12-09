// Package middleware internal/middleware/cors.go
package middleware

import "net/http"

func CORS(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		// Allow all origins (adjust if you want a whitelist)
		w.Header().Set("Access-Control-Allow-Origin", "*")

		// Allowed methods
		w.Header().Set("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS")

		// Allowed headers (added AppCheck explicitly)
		w.Header().Set("Access-Control-Allow-Headers", "Content-Type, X-Firebase-AppCheck, Authorization")

		// Cache preflight results
		w.Header().Set("Access-Control-Max-Age", "3600")

		// Handle preflight immediately
		if r.Method == http.MethodOptions {
			w.WriteHeader(http.StatusNoContent)
			return
		}

		// Normal request
		next.ServeHTTP(w, r)
	})
}
