import { createApiApp } from "./bootstrap/createApiApp.js";

let cachedApp: ReturnType<typeof createApiApp> | null = null;

export function buildApp() {
  if (!cachedApp) {
    cachedApp = createApiApp();
  }
  return cachedApp;
}
