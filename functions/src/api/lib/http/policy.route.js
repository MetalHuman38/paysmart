import { corsify } from "../utils.js";
import { requireAppCheck } from "../config/appcheck.js";
import { requireActiveSession } from "../middleware/requireActiveSession.js";
// 🔐 Clean-architecture handlers
import { getSecuritySettingsHandler } from "../handlers/getSecuritySettings.js";
import { biometricsEnabledHandler } from "../handlers/biometricEnabled.js";
import { passcodeEnabledHandler } from "../handlers/passcodeEnabled.js";
import { passwordEnabledHandler } from "../handlers/passwordEnabled.js";
import { allowFederatedLinkingHandler } from "../handlers/allowFederatedLinking.js";
import { checkEmailVerificationStatusHandler, generateEmailVerificationHandler } from "../handlers/emailVerificationHandlers.js";
import { checkPhoneAvailabilityHandler } from "../handlers/checkPhoneAvailabilityHandler.js";
import { confirmPhoneChangedHandler } from "../handlers/confirmPhoneChanged.js";
export function mountAuthPolicyRoutes(app) {
    // Email Verification
    app.post("/auth/generate", requireActiveSession, generateEmailVerificationHandler);
    app.post("/auth/status", requireActiveSession, checkEmailVerificationStatusHandler);
    // ---- Password / Passcode / Biometrics ----
    app.post("/auth/setPasswordEnabled", requireActiveSession, passwordEnabledHandler);
    app.options("/auth/setPasswordEnabled", (_, res) => {
        corsify(res);
        res.status(204).end();
    });
    app.post("/auth/setPasscodeEnabled", requireActiveSession, passcodeEnabledHandler);
    app.options("/auth/setPasscodeEnabled", (_, res) => {
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
    // ---- Read-only (critical) ----
    app.get("/auth/getSecuritySettings", requireAppCheck, requireActiveSession, getSecuritySettingsHandler);
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
//# sourceMappingURL=policy.route.js.map