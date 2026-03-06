import { Request, Response } from "express";
import { authContainer } from "../infrastructure/di/authContainer.js";
import { logPasskeyAuditEvent } from "./passkeyAudit.js";

export async function passkeySignInOptionsHandler(req: Request, res: Response) {
  logPasskeyAuditEvent(req, "signin_options_attempt");
  try {
    const { passkeys } = authContainer();
    const options = await passkeys.beginSignIn();
    logPasskeyAuditEvent(req, "signin_options_success");
    return res.status(200).json({ options });
  } catch (error) {
    const message = error instanceof Error ? error.message : "Internal error";
    logPasskeyAuditEvent(req, "signin_options_failure", {
      error: message,
      code: message.includes("PASSKEY_NOT_CONFIGURED") ? "PASSKEY_NOT_CONFIGURED" : "INTERNAL_ERROR",
    });
    if (message.includes("PASSKEY_NOT_CONFIGURED")) {
      return res.status(503).json({
        error:
          "Passkey service is not configured. Set PASSKEY_RP_ID and PASSKEY_EXPECTED_ORIGINS (or PASSKEY_ANDROID_APK_KEY_HASHES). Android hashes must be Base64URL without padding.",
        code: "PASSKEY_NOT_CONFIGURED",
      });
    }

    console.error("passkeySignInOptionsHandler failed", error);
    return res.status(500).json({ error: "Internal error" });
  }
}
