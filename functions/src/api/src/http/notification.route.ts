import type { Express } from "express";
import { requireAppCheck } from "../config/appcheck.js";
import { emailUnsubscribeHandler } from "../handlers/emailUnsubscribe.js";
import { getNotificationPreferencesHandler } from "../handlers/getNotificationPreferences.js";
import { registerNotificationInstallationHandler } from "../handlers/registerNotificationInstallation.js";
import { updateNotificationPreferencesHandler } from "../handlers/updateNotificationPreferences.js";
import { requireActiveSession } from "../middleware/requireActiveSession.js";
import { corsify } from "../utils.js";

export function mountNotificationRoutes(app: Express) {
  app.post(
    "/notifications/installations/register",
    requireAppCheck,
    requireActiveSession,
    registerNotificationInstallationHandler
  );
  app.options("/notifications/installations/register", (_, res) => {
    corsify(res);
    res.status(204).end();
  });

  app.get(
    "/notifications/preferences",
    requireAppCheck,
    requireActiveSession,
    getNotificationPreferencesHandler
  );
  app.options("/notifications/preferences", (_, res) => {
    corsify(res);
    res.status(204).end();
  });

  app.post(
    "/notifications/preferences",
    requireAppCheck,
    requireActiveSession,
    updateNotificationPreferencesHandler
  );

  app.get("/email/unsubscribe", emailUnsubscribeHandler);
}
