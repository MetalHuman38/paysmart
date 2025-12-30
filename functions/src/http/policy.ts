import type { Express } from "express";
import { corsify } from "../utils";
import { requireAppCheck } from "../config/appcheck";
import { getPasswordEnabledHandler, setPasswordEnabledHandler } from "../passwordenabledflag";
import {
  generateEmailVerificationHandler,
  checkEmailVerificationStatusHandler,
} from "../emailVerificationHandlers.js";
import { checkPhoneAvailability } from "./checkPhoneAvailability";

export function mountPolicyRoutes(app: Express) {
  // Set Password Enabled
  app.post("/auth/setPasswordEnabled", requireAppCheck, setPasswordEnabledHandler);
  app.options("/auth/setPasswordEnabled", (req, res) => {
    res.status(204).end();
  });

  // Get Password Enabled
  app.get("/auth/getPasswordEnabled", requireAppCheck, getPasswordEnabledHandler);

  app.post("/auth/generate", generateEmailVerificationHandler);
  app.post("/auth/status", checkEmailVerificationStatusHandler);

  app.post("/auth/check-phone", requireAppCheck, checkPhoneAvailability);
  app.options("/auth/check-phone", (req, res) => {
    corsify(res);
    res.status(204).end();
  });


  app.options("/auth/check-email-or-phone", (req, res) => {
    corsify(res);
    res.status(204).end();
  });
}
