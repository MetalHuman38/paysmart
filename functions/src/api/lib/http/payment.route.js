import express from "express";
import { corsify } from "../utils.js";
import { requireActiveSession } from "../middleware/requireActiveSession.js";
import { createAddMoneySessionHandler } from "../handlers/createAddMoneySession.js";
import { getAddMoneySessionStatusHandler } from "../handlers/getAddMoneySessionStatus.js";
import { stripeWebhookHandler } from "../handlers/stripeWebhook.js";
import { createFlutterwaveAddMoneySessionHandler } from "../handlers/createFlutterwaveAddMoneySession.js";
import { getFlutterwaveAddMoneySessionStatusHandler } from "../handlers/getFlutterwaveAddMoneySessionStatus.js";
import { flutterwaveWebhookHandler } from "../handlers/flutterwaveWebhook.js";
export function mountPaymentsWebhookRoute(app) {
    app.post("/payments/stripe/webhook", express.raw({ type: "application/json", limit: "1mb" }), stripeWebhookHandler);
    app.options("/payments/stripe/webhook", (_, res) => {
        corsify(res);
        res.status(204).end();
    });
    app.post("/payments/flutterwave/webhook", express.raw({ type: "application/json", limit: "1mb" }), flutterwaveWebhookHandler);
    app.options("/payments/flutterwave/webhook", (_, res) => {
        corsify(res);
        res.status(204).end();
    });
}
export function mountPaymentsRoutes(app) {
    app.post("/payments/add-money/session", requireActiveSession, createAddMoneySessionHandler);
    app.options("/payments/add-money/session", (_, res) => {
        corsify(res);
        res.status(204).end();
    });
    app.get("/payments/add-money/session/:sessionId", requireActiveSession, getAddMoneySessionStatusHandler);
    app.options("/payments/add-money/session/:sessionId", (_, res) => {
        corsify(res);
        res.status(204).end();
    });
    app.post("/payments/flutterwave/add-money/session", requireActiveSession, createFlutterwaveAddMoneySessionHandler);
    app.options("/payments/flutterwave/add-money/session", (_, res) => {
        corsify(res);
        res.status(204).end();
    });
    app.get("/payments/flutterwave/add-money/session/:sessionId", requireActiveSession, getFlutterwaveAddMoneySessionStatusHandler);
    app.options("/payments/flutterwave/add-money/session/:sessionId", (_, res) => {
        corsify(res);
        res.status(204).end();
    });
}
//# sourceMappingURL=payment.route.js.map