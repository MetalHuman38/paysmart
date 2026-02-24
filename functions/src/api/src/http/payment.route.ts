import express, { type Express } from "express";
import { corsify } from "../utils.js";
import { requireActiveSession } from "../middleware/requireActiveSession.js";
import { createAddMoneySessionHandler } from "../handlers/createAddMoneySession.js";
import { getAddMoneySessionStatusHandler } from "../handlers/getAddMoneySessionStatus.js";
import { stripeWebhookHandler } from "../handlers/stripeWebhook.js";

export function mountPaymentsWebhookRoute(app: Express) {
  app.post(
    "/payments/stripe/webhook",
    express.raw({ type: "application/json", limit: "1mb" }),
    stripeWebhookHandler
  );
  app.options("/payments/stripe/webhook", (_, res) => {
    corsify(res);
    res.status(204).end();
  });
}

export function mountPaymentsRoutes(app: Express) {
  app.post(
    "/payments/add-money/session",
    requireActiveSession,
    createAddMoneySessionHandler
  );
  app.options("/payments/add-money/session", (_, res) => {
    corsify(res);
    res.status(204).end();
  });

  app.get(
    "/payments/add-money/session/:sessionId",
    requireActiveSession,
    getAddMoneySessionStatusHandler
  );
  app.options("/payments/add-money/session/:sessionId", (_, res) => {
    corsify(res);
    res.status(204).end();
  });
}
