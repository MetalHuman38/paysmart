// src/deps.ts
import admin from "firebase-admin";
import type { Config } from "./config/configuration";
import { loadConfig } from "./config/configuration";

export type Deps = {
  getConfig(): Config;
  app: admin.app.App;
  auth: admin.auth.Auth;
  firestore: FirebaseFirestore.Firestore;
};

let singleton: Deps | null = null;

export function initDeps(): Deps {
  if (singleton) return singleton;

  // 1️⃣ Admin SDK init (SAFE at startup)
  if (!admin.apps.length) {
    admin.initializeApp();
  }

  const app = admin.app();
  const auth = admin.auth(app);
  const firestore = admin.firestore(app);

  // 2️⃣ Firestore emulator config (SAFE)
  if (process.env.FIRESTORE_EMULATOR_HOST) {
    firestore.settings({
      host: process.env.FIRESTORE_EMULATOR_HOST,
      ssl: false,
      ignoreUndefinedProperties: true,
    });
  } else {
    firestore.settings({ ignoreUndefinedProperties: true });
  }

  // 3️⃣ Config loader is LAZY
  let cachedConfig: Config | null = null;

  function getConfig(): Config {
    if (!cachedConfig) {
      cachedConfig = loadConfig();
    }
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
