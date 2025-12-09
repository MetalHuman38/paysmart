package main

import (
	"log"
	"os"
	_ "paysmart_go"

	"github.com/GoogleCloudPlatform/functions-framework-go/funcframework"
)

func main() {
	// Ensure FUNCTION_TARGET matches what you registered: "api"
	if os.Getenv("FUNCTION_TARGET") == "" {
		_ = os.Setenv("FUNCTION_TARGET", "api")
	}
	if os.Getenv("BASE_URL") == "" {
		_ = os.Setenv("BASE_URL", "http://192.168.56.1:8080")
	}
	port := os.Getenv("PORT")
	if port == "" {
		port = "8080"
	}

	log.Printf("Starting Functions Framework on port %s (FUNCTION_TARGET=%s)", port, os.Getenv("FUNCTION_TARGET"))
	if err := funcframework.Start(port); err != nil {
		log.Fatalf("funcframework.Start: %v", err)
	}
}
