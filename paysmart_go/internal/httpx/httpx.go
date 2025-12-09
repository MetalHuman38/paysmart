package httpx

import (
	"net/http"

	"github.com/goccy/go-json"
)

func CORS(w http.ResponseWriter) {
	w.Header().Set("Access-Control-Allow-Origin", "*")
	w.Header().Set("Access-Control-Allow-Methods", "POST,OPTIONS")
	w.Header().Set("Access-Control-Allow-Headers", "Content-Type,Authorization")
}

func JSON(w http.ResponseWriter, status int, v any) {
	w.Header().Set("Content-Type", "application/json")
	CORS(w)
	w.WriteHeader(status)
	_ = json.NewEncoder(w).Encode(v)
}

func Deny(w http.ResponseWriter, status int, code, msg string) {
	JSON(w, status, map[string]any{
		"error": map[string]any{
			"code":    code,
			"message": msg,
		},
	})
}
