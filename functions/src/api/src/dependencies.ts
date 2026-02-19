// src/dependencies.ts
import { initializeApp, getApps, App } from "firebase-admin/app";
import { getAuth, Auth } from "firebase-admin/auth";
import { getFirestore, Firestore } from "firebase-admin/firestore";

import type { Config } from "./config/configuration.js";
import { loadConfig } from "./config/configuration.js";

export type Deps = {
  getConfig(): Config;
  app: App;
  auth: Auth;
  firestore: Firestore;
};

let singleton: Deps | null = null;

export function initDeps(): Deps {
  if (singleton) return singleton;

  // ✅ Explicit, safe init — no getApp()
  let app: App;
  const apps = getApps();

  if (apps.length === 0) {
    app = initializeApp();
  } else {
    app = apps[0];
  }

  const auth = getAuth(app);
  const firestore = getFirestore(app);

  if (process.env.FIRESTORE_EMULATOR_HOST) {
    firestore.settings({
      host: process.env.FIRESTORE_EMULATOR_HOST,
      ssl: false,
      ignoreUndefinedProperties: true,
    });
  } else {
    firestore.settings({
      ignoreUndefinedProperties: true,
    });
  }

  let cachedConfig: Config | null = null;
  function getConfig(): Config {
    if (!cachedConfig) cachedConfig = loadConfig();
    return cachedConfig;
  }

  singleton = {
    getConfig,
    app,
    auth,
    firestore,
  };

  return singleton;
}
