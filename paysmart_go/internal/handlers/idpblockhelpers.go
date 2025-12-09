// Package handlers internal/handlers/idpblock.go
package handlers

// ---------- local wire types & helpers (replace missing ones) ----------

type beforeBody struct {
	EventType string         `json:"eventType"`
	Resource  string         `json:"resource"`
	Data      map[string]any `json:"data"`
}

func getUserMap(b beforeBody) map[string]any {
	if b.Data == nil {
		return map[string]any{}
	}
	if u, ok := b.Data["userInfo"].(map[string]any); ok && u != nil {
		return u
	}
	if u, ok := b.Data["user"].(map[string]any); ok && u != nil {
		return u
	}
	return map[string]any{}
}
