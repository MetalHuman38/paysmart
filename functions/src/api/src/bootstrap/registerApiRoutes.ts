import type { Express } from "express";
import { mountHealthRoutes } from "../http/health.route.js";
import { mountRecaptchaRoutes } from "../http/recaptcha.route.js";
import { mountAuthPolicyRoutes } from "../http/policy.route.js";
import { mountPhoneCheckRoutes } from "../http/policy.js";
import { mountFxRoutes } from "../http/fx.route.js";
import {
  mountPaymentsRoutes,
  mountPaymentsWebhookRoute,
} from "../http/payment.route.js";
import { mountInvoiceRoutes } from "../http/invoice.route.js";
import { mountAdminRoutes } from "../http/admin.route.js";
import { mountNotificationRoutes } from "../http/notification.route.js";
import { mountPublicRoutes } from "../http/public.route.js";
import { facebookDataDeletionHandler } from "../facebookDataDeletion.js";
import { requireAppCheck } from "../config/appcheck.js";
import { checkEmailOrPhone } from "../checkEmailOrPhone.js";

export function registerPreMiddlewareRoutes(app: Express) {
  mountPaymentsWebhookRoute(app);
}

export function registerApiRoutes(app: Express) {
  mountPublicRoutes(app);
  mountAuthPolicyRoutes(app);
  mountHealthRoutes(app);
  mountRecaptchaRoutes(app);
  mountFxRoutes(app);
  mountPhoneCheckRoutes(app);
  mountPaymentsRoutes(app);
  mountInvoiceRoutes(app);
  mountAdminRoutes(app);
  mountNotificationRoutes(app);
}

export function registerPostBodyParserRoutes(app: Express) {
  app.post("/auth/check-email-or-phone", requireAppCheck, checkEmailOrPhone);

  app.post("/facebook/data-deletion", facebookDataDeletionHandler);
  app.get("/", (_, res) => {
    return res.status(200).json({ ok: true, service: "api" });
  });
  app.get("/favicon.ico", (_, res) => {
    res.status(204).end();
  });
}

export function registerFallbackRoutes(app: Express) {
  app.use((req, res) => {
    if (process.env.LOG_404 === "true") {
      console.warn(`404 Not Found: ${req.method} ${req.path}`);
    }
    res.status(404).json({ error: "Not Found" });
  });
}
