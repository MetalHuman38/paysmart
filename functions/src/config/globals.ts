// C:\Users\Metal\paysmart\functions\src\config\globals.ts
export const APP = {
  name: "PaySmart Functions",
  env: (process.env.NODE_ENV || "development") as "development" | "production" | "test",
  region: "europe-west2" as const,
  projectId: process.env.GCLOUD_PROJECT || process.env.GCP_PROJECT || "",
  // Enable simple req logging if you want: LOG_REQUESTS=true
  logRequests: (process.env.LOG_REQUESTS || "false").toLowerCase() === "true",
  // Emulator flag from Firebase tooling
  emulator: (process.env.FUNCTIONS_EMULATOR || "").toLowerCase() === "true"
};

// Central security knobs
const DEFAULT_ALLOWED = [
  "http://localhost:5000",
  "http://127.0.0.1:5000",
  "http://localhost:5173",
  "http://127.0.0.1:5173",
  "http://localhost:8080",
  "http://127.0.0.1:8080",
  "https://voltpay.metalbrain.net",
  "https://metalbrain.net"
];

// Security configuration loaded from environment variables
export const SECURITY = {
  allowedOrigins: (process.env.CORS_ALLOWED_ORIGINS || "")
    .split(",")
    .map(s => s.trim())
    .filter(Boolean)
    .concat(DEFAULT_ALLOWED)
    .filter((v, i, a) => a.indexOf(v) === i),
  allowCredentials: true,

  // App Check
  // APP_CHECK_REQUIRED=true to enforce in prod; emulator bypasses by default
  appCheckRequired: (process.env.APP_CHECK_REQUIRED || "true").toLowerCase() === "true",

  // Rate limit budget (per IP)
  rateLimit: {
    windowMs: Number(process.env.RATE_WINDOW_MS || 60_000),   // 1 minute
    max: Number(process.env.RATE_MAX_REQUESTS || 120),                 // 120 req/min
    // you can tune per-route overrides too
    authWindowMs: Number(process.env.RATE_AUTH_WINDOW_MS || 60_000),
    authMax: Number(process.env.RATE_AUTH_MAX_REQUESTS || 30)
  }
};
