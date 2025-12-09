package auth

import (
	"context"
	"fmt"
	"os"
	"path/filepath"
	"paysmart_go/internal/config"

	"cloud.google.com/go/firestore"
	firebase "firebase.google.com/go/v4"
	"firebase.google.com/go/v4/auth"
	"google.golang.org/api/option"

	_ "paysmart_go/internal/config"
)

type Deps struct {
	Cfg       *config.Config
	App       *firebase.App
	Auth      *auth.Client
	AuthGate  *config.Config
	Firestore *firestore.Client
}

func Init(ctx context.Context, cfg *config.Config) (*Deps, error) {
	var opts []option.ClientOption

	// 1) Emulator? -> no auth required
	if emu := os.Getenv("FIRESTORE_EMULATOR_HOST"); emu != "" {
		// Ensure project id is present
		projectID := cfg.ProjectID
		if projectID == "" {
			projectID = os.Getenv("GCP_PROJECT")
		}
		if projectID == "" {
			projectID = os.Getenv("GOOGLE_CLOUD_PROJECT")
		}
		if projectID == "" {
			projectID = "demo-project"
		}

		// IMPORTANT: Use WithoutAuthentication for emulator
		opts = append(opts, option.WithoutAuthentication())

		app, err := firebase.NewApp(ctx, &firebase.Config{ProjectID: projectID}, opts...)
		if err != nil {
			return nil, fmt.Errorf("firebase app (emu): %w", err)
		}
		au, err := app.Auth(ctx)
		if err != nil {
			return nil, fmt.Errorf("auth (emu): %w", err)
		}
		fs, err := app.Firestore(ctx)
		if err != nil {
			return nil, fmt.Errorf("firestore (emu): %w", err)
		}
		return &Deps{Cfg: cfg, App: app, Auth: au, Firestore: fs}, nil
	}

	// 2) Service account file? -> only if file exists
	if cred := os.Getenv("GOOGLE_APPLICATION_CREDENTIALS"); cred != "" {
		if _, err := os.Stat(cred); err == nil {
			abs, _ := filepath.Abs(cred)
			opts = append(opts, option.WithCredentialsFile(abs))
		} else {
			// Bad path – IGNORE it and fall back to ADC instead of failing
			// (or: return error if you want to enforce it)
			fmt.Printf("[auth] WARNING: GOOGLE_APPLICATION_CREDENTIALS not found: %s (falling back to ADC)\n", cred)
		}
	}

	// 3) Default ADC (Cloud Run/Functions metadata credentials, or local gcloud ADC)
	projectID := cfg.ProjectID
	if projectID == "" {
		projectID = os.Getenv("GCP_PROJECT")
	}
	if projectID == "" {
		projectID = os.Getenv("GOOGLE_CLOUD_PROJECT")
	}

	app, err := firebase.NewApp(ctx, &firebase.Config{ProjectID: projectID}, opts...)
	if err != nil {
		return nil, fmt.Errorf("firebase app: %w", err)
	}
	au, err := app.Auth(ctx)
	if err != nil {
		return nil, fmt.Errorf("auth: %w", err)
	}
	fs, err := app.Firestore(ctx)
	if err != nil {
		return nil, fmt.Errorf("firestore: %w", err)
	}
	return &Deps{Cfg: cfg, App: app, Auth: au, Firestore: fs}, nil
}
