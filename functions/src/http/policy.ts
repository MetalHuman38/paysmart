import type { Express } from "express";
import { corsify } from "../utils";
import { requireAppCheck } from "../config/appcheck";
import { getPasswordEnabledHandler, setPasswordEnabledHandler } from "../passwordenabledflag";
import {
  generateEmailVerificationHandler,
  checkEmailVerificationStatusHandler,
} from "../emailVerificationHandlers.js";
import { checkPhoneAvailability } from "./checkPhoneAvailability";
import { getPassCodeEnabledHandler, setPassCodeEnabledHandler } from "../passcodeEnabledflag";
import { getBiometricsRequiredHandler, biometricsRequiredHandler } from "../biometricEnabled";

export function mountPolicyRoutes(app: Express) {
  // Set Password Enabled
  app.post("/auth/setPasswordEnabled", requireAppCheck, setPasswordEnabledHandler);
  app.options("/auth/setPasswordEnabled", (req, res) => {
    res.status(204).end();
  });

  // Get Password Enabled
  app.get("/auth/getPasswordEnabled", requireAppCheck, getPasswordEnabledHandler);

  // Set Passcode Enabled
  app.post("/auth/setPassCodeEnabled", requireAppCheck, setPassCodeEnabledHandler);
  app.options("/auth/setPassCodeEnabled", (req, res) => {
    res.status(204).end();
  });
  // Get Passcode Enabled
  app.get("/auth/getPassCodeEnabled", requireAppCheck, getPassCodeEnabledHandler);

  // Biometrics Required
  // Set Biometrics Required
  // Get Biometrics Required
  app.post("/auth/setBiometricEnabled", requireAppCheck,   biometricsRequiredHandler);

  app.options("/auth/setBiometricEnabled", (req, res) => {
    res.status(204).end();
  });
  app.get("/auth/getBiometricsRequired", requireAppCheck, getBiometricsRequiredHandler);


  // Email Verification
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
