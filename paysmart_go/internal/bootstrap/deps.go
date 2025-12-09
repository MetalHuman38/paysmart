// internal/bootstrap/deps.go

package bootstrap

import (
	"context"
	"sync"

	"paysmart_go/internal/auth"
	"paysmart_go/internal/config"
)

var (
	once    sync.Once
	global  *auth.Deps
	initErr error
)

func Deps(ctx context.Context) (*auth.Deps, error) {
	once.Do(func() {
		cfg := config.Load()
		global, initErr = auth.Init(ctx, cfg)
	})
	return global, initErr
}
