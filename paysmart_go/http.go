// Package function/http.go (your entry package)
package function

import (
	"fmt"
	"net/http"
	"os"
	"paysmart_go/internal/handlers"
	"time"

	"github.com/GoogleCloudPlatform/functions-framework-go/functions"
	"github.com/joho/godotenv"
)

func init() {
	_ = godotenv.Load(".env")

	router := handlers.NewRouter(handlers.RouterDeps{
		HTTPClient: &http.Client{Timeout: 10 * time.Second},
	})

	// Single registration
	functions.HTTP("api", func(w http.ResponseWriter, req *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		router.ServeHTTP(w, req)
	})

	port := os.Getenv("PORT")
	if port == "" {
		port = "8080"
	}
	fmt.Println("[boot] Functions Framework will listen on PORT:", port)
}
