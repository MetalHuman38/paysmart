// src/dependencies.ts
import { initializeApp, getApps } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import { getFirestore } from "firebase-admin/firestore";
import { loadConfig } from "./config/configuration.js";
let singleton = null;
export function initDeps() {
    if (singleton)
        return singleton;
    // ✅ Explicit, safe init — no getApp()
    let app;
    const apps = getApps();
    if (apps.length === 0) {
        app = initializeApp();
    }
    else {
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
    }
    else {
        firestore.settings({
            ignoreUndefinedProperties: true,
        });
    }
    let cachedConfig = null;
    function getConfig() {
        if (!cachedConfig)
            cachedConfig = loadConfig();
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
//# sourceMappingURL=dependencies.js.map