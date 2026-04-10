import type { Express } from "express";
import { corsify } from "../utils.js";
import { requireAppCheck } from "../config/appcheck.js";
import { requireActiveSession } from "../middleware/requireActiveSession.js";

// 🔐 Clean-architecture handlers
import { getSecuritySettingsHandler } from "../handlers/getSecuritySettings.js";
import { biometricsEnabledHandler } from "../handlers/biometricEnabled.js";
import { passwordEnabledHandler } from "../handlers/passwordEnabled.js";
import { allowFederatedLinkingHandler } from "../handlers/allowFederatedLinking.js";
import { checkEmailVerificationStatusHandler, generateEmailVerificationHandler } from "../handlers/emailVerificationHandlers.js";
import { checkPhoneAvailabilityHandler } from "../handlers/checkPhoneAvailabilityHandler.js";
import { confirmPhoneChangedHandler } from "../handlers/confirmPhoneChanged.js";
import { finalizePhoneSignupHandler } from "../handlers/finalizePhoneSignup.js";
import { lookupAddressHandler } from "../handlers/lookupAddress.js";
import { setHomeAddressVerifiedHandler } from "../handlers/setHomeAddressVerified.js";
import { setMfaEnrollmentPromptStateHandler } from "../handlers/setMfaEnrollmentPromptState.js";
import { identityUploadSessionHandler } from "../handlers/identityUploadSession.js";
import { identityUploadCommitHandler } from "../handlers/identityUploadCommit.js";
import { identityUploadPayloadHandler } from "../handlers/identityUploadPayload.js";
import { identityExtractTextHandler } from "../handlers/identityExtractText.js";
import { identityProviderStartSessionHandler } from "../handlers/identityProviderStartSession.js";
import { identityProviderResumeHandler } from "../handlers/identityProviderResume.js";
import { identityProviderCallbackHandler } from "../handlers/identityProviderCallback.js";
import { passkeyRegisterOptionsHandler } from "../handlers/passkeyRegisterOptions.js";
import { passkeyRegisterVerifyHandler } from "../handlers/passkeyRegisterVerify.js";
import { passkeyAuthenticateOptionsHandler } from "../handlers/passkeyAuthenticateOptions.js";
import { passkeyAuthenticateVerifyHandler } from "../handlers/passkeyAuthenticateVerify.js";
import { passkeySignInOptionsHandler } from "../handlers/passkeySignInOptions.js";
import { passkeySignInVerifyHandler } from "../handlers/passkeySignInVerify.js";
import { passkeyListCredentialsHandler } from "../handlers/passkeyListCredentials.js";
import { passkeyRevokeCredentialHandler } from "../handlers/passkeyRevokeCredential.js";
import { setPasskeyEnabledHandler } from "../handlers/setPasskeyEnabled.js";
import {
  passkeySignInOptionsRateLimit,
  passkeySignInVerifyRateLimit,
} from "../middleware/passkeySignInRateLimit.js";

export function mountAuthPolicyRoutes(app: Express) {

  // Email Verification
  app.post("/auth/generate", requireActiveSession, generateEmailVerificationHandler);
  app.post("/auth/status", requireActiveSession, checkEmailVerificationStatusHandler);
  // ---- Password / Biometrics ----
    app.post("/auth/setPasswordEnabled", requireActiveSession, passwordEnabledHandler);
    app.options("/auth/setPasswordEnabled", (_, res) => {
    corsify(res);
    res.status(204).end();
  });

  app.post("/auth/setBiometricEnabled", requireActiveSession, biometricsEnabledHandler);
  app.options("/auth/setBiometricEnabled", (_, res) => {
    corsify(res);
    res.status(204).end();
  });

  app.post("/auth/allowFederatedLinking", requireActiveSession, allowFederatedLinkingHandler);
  app.options("/auth/allowFederatedLinking", (_, res) => {
    corsify(res);
    res.status(204).end();
  });

  app.post("/auth/confirmPhoneChanged", requireActiveSession, confirmPhoneChangedHandler);
  app.options("/auth/confirmPhoneChanged", (_, res) => {
    corsify(res);
    res.status(204).end();
  });

  app.post("/auth/finalize-phone-signup", requireAppCheck, finalizePhoneSignupHandler);
  app.options("/auth/finalize-phone-signup", (_, res) => {
    corsify(res);
    res.status(204).end();
  });

  app.post("/auth/lookupAddress", requireActiveSession, lookupAddressHandler);
  app.options("/auth/lookupAddress", (_, res) => {
    corsify(res);
    res.status(204).end();
  });

  app.post("/auth/setHomeAddressVerified", requireActiveSession, setHomeAddressVerifiedHandler);
  app.options("/auth/setHomeAddressVerified", (_, res) => {
    corsify(res);
    res.status(204).end();
  });

  app.post(
    "/auth/setMfaEnrollmentPromptState",
    requireActiveSession,
    setMfaEnrollmentPromptStateHandler
  );
  app.options("/auth/setMfaEnrollmentPromptState", (_, res) => {
    corsify(res);
    res.status(204).end();
  });

  app.post("/auth/identity/upload/session", requireActiveSession, identityUploadSessionHandler);
  app.options("/auth/identity/upload/session", (_, res) => {
    corsify(res);
    res.status(204).end();
  });

  app.post("/auth/identity/upload/commit", requireActiveSession, identityUploadCommitHandler);
  app.options("/auth/identity/upload/commit", (_, res) => {
    corsify(res);
    res.status(204).end();
  });

  app.post("/auth/identity/upload/payload", requireActiveSession, identityUploadPayloadHandler);
  app.options("/auth/identity/upload/payload", (_, res) => {
    corsify(res);
    res.status(204).end();
  });

  app.post(
    "/auth/identity/extractText",
    requireActiveSession,
    identityExtractTextHandler
  );
  app.options("/auth/identity/extractText", (_, res) => {
    corsify(res);
    res.status(204).end();
  });

  app.post(
    "/auth/identity/provider/startSession",
    requireActiveSession,
    identityProviderStartSessionHandler
  );
  app.options("/auth/identity/provider/startSession", (_, res) => {
    corsify(res);
    res.status(204).end();
  });

  app.post(
    "/auth/identity/provider/resume",
    requireActiveSession,
    identityProviderResumeHandler
  );
  app.options("/auth/identity/provider/resume", (_, res) => {
    corsify(res);
    res.status(204).end();
  });

  app.post(
    "/auth/identity/provider/callback",
    requireActiveSession,
    identityProviderCallbackHandler
  );
  app.options("/auth/identity/provider/callback", (_, res) => {
    corsify(res);
    res.status(204).end();
  });

  app.post(
    "/auth/passkeys/register/options",
    requireActiveSession,
    passkeyRegisterOptionsHandler
  );
  app.options("/auth/passkeys/register/options", (_, res) => {
    corsify(res);
    res.status(204).end();
  });

  app.post(
    "/auth/passkeys/register/verify",
    requireActiveSession,
    passkeyRegisterVerifyHandler
  );
  app.options("/auth/passkeys/register/verify", (_, res) => {
    corsify(res);
    res.status(204).end();
  });

  app.post(
    "/auth/passkeys/authenticate/options",
    requireActiveSession,
    passkeyAuthenticateOptionsHandler
  );
  app.options("/auth/passkeys/authenticate/options", (_, res) => {
    corsify(res);
    res.status(204).end();
  });

  app.post(
    "/auth/passkeys/authenticate/verify",
    requireActiveSession,
    passkeyAuthenticateVerifyHandler
  );
  app.options("/auth/passkeys/authenticate/verify", (_, res) => {
    corsify(res);
    res.status(204).end();
  });

  app.post("/auth/setPasskeyEnabled", requireActiveSession, setPasskeyEnabledHandler);
  app.options("/auth/setPasskeyEnabled", (_, res) => {
    corsify(res);
    res.status(204).end();
  });

  app.post("/auth/passkeys/revoke", requireActiveSession, passkeyRevokeCredentialHandler);
  app.options("/auth/passkeys/revoke", (_, res) => {
    corsify(res);
    res.status(204).end();
  });

  app.get("/auth/passkeys/list", requireActiveSession, passkeyListCredentialsHandler);
  app.options("/auth/passkeys/list", (_, res) => {
    corsify(res);
    res.status(204).end();
  });

  app.post(
    "/auth/passkeys/signin/options",
    requireAppCheck,
    passkeySignInOptionsRateLimit,
    passkeySignInOptionsHandler
  );
  app.options("/auth/passkeys/signin/options", (_, res) => {
    corsify(res);
    res.status(204).end();
  });

  app.post(
    "/auth/passkeys/signin/verify",
    requireAppCheck,
    passkeySignInVerifyRateLimit,
    passkeySignInVerifyHandler
  );
  app.options("/auth/passkeys/signin/verify", (_, res) => {
    corsify(res);
    res.status(204).end();
  });

  // ---- Read-only (critical) ----

  app.get(
    "/auth/getSecuritySettings",
    requireAppCheck,
    requireActiveSession,
    getSecuritySettingsHandler
  );
  app.options("/auth/getSecuritySettings", (_, res) => {
    corsify(res);
    res.status(204).end();
  });
    app.post("/auth/check-phone", requireAppCheck, checkPhoneAvailabilityHandler);
    app.options("/auth/check-phone", (req, res) => {
      corsify(res);
      res.status(204).end();
    });
  
    app.options("/auth/check-email-or-phone", (req, res) => {
      corsify(res);
      res.status(204).end();
    });
}
