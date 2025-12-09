package handlers

import (
	"net/http"

	"paysmart_go/internal/middleware"

	"github.com/go-chi/chi/v5"
	chimid "github.com/go-chi/chi/v5/middleware"
)

type RouterDeps struct {
	HTTPClient *http.Client
}

func NewRouter(deps RouterDeps) http.Handler {
	r := chi.NewRouter()

	// ✅ Middlewares
	r.Use(chimid.RequestID)
	r.Use(chimid.RealIP)
	r.Use(chimid.Recoverer)
	r.Use(chimid.StripSlashes)
	r.Use(middleware.CORS)

	// ✅ Main API routes
	r.Route("/api", func(api chi.Router) {
		api.Get("/health", health)

		// Simple ping
		api.Get("/message", func(w http.ResponseWriter, _ *http.Request) {
			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusOK)
			_, _ = w.Write([]byte(`{"message":"Hello from PaySmart Go backend"}`))
		})

		// ✅ Mount domain-specific routes under /api
		Mount(api, nil)
	})

	// ✅ 404 fallback (for unmatched routes)
	r.NotFound(func(w http.ResponseWriter, req *http.Request) {
		http.Error(w, "Route not found", http.StatusNotFound)
	})

	return r
}
