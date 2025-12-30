import * as admin from "firebase-admin";
import { onRequest, onCall } from "firebase-functions/v2/https";
import { APP } from "./config/globals.js";
import { buildApp } from "./server.js";
import { logger } from "firebase-functions";
import { RESEND_API_KEY } from "./config/params.js";

// Initialize Admin SDK exactly once
if (admin.apps.length === 0) {
  admin.initializeApp();
}


// HTTP API function
export const api = onRequest(
  {
    region: APP.region,
    secrets: [RESEND_API_KEY],
    cors: false,
  },
  (req, res) => {
    const app = buildApp();
    return app(req, res);
  }
);

// Auth Blocking Triggers
export { beforeCreate } from "./triggers/beforeCreate.trigger.js";
export { beforeSignIn } from "./triggers/beforeSignIn.trigger.js";

// Security Firestore Trigger
export { seedSecurityOnUserCreate, ensureSecurityDoc } from "./security.js";

// Simple echo callable function for testing
export const echo = onCall(
  {
    region: APP.region,
    enforceAppCheck: true
  },
  (req) => {
    return { data: req.data, appId: req.app?.appId || null };
  }
);

logger.info(`Function "api" initialized in region: ${APP.region}`);
