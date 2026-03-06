import { Request, Response } from "express";
import { initDeps } from "../dependencies.js";
import { authContainer } from "../infrastructure/di/authContainer.js";

export async function passkeyRegisterOptionsHandler(req: Request, res: Response) {
  try {
    const authHeader = req.headers.authorization;
    if (!authHeader?.startsWith("Bearer ")) {
      return res.status(401).json({ error: "Missing token" });
    }

    const idToken = authHeader.split("Bearer ")[1];
    const { auth } = initDeps();
    const decoded = await auth.verifyIdToken(idToken);

    const userName = (req.body?.userName ?? "").toString().trim() || decoded.uid;
    const userDisplayName =
      (req.body?.userDisplayName ?? "").toString().trim() || userName;

    const { passkeys } = authContainer();
    const options = await passkeys.beginRegistration(decoded.uid, {
      userName,
      userDisplayName,
    });

    return res.status(200).json({ options });
  } catch (error) {
    const message = error instanceof Error ? error.message : "Internal error";
    if (message.includes("PASSKEY_NOT_CONFIGURED")) {
      return res.status(503).json({
        error:
          "Passkey service is not configured. Set PASSKEY_RP_ID and PASSKEY_EXPECTED_ORIGINS (or PASSKEY_ANDROID_APK_KEY_HASHES). Android hashes must be Base64URL without padding.",
        code: "PASSKEY_NOT_CONFIGURED",
      });
    }
    console.error("passkeyRegisterOptionsHandler failed", error);
    return res.status(500).json({ error: "Internal error" });
  }
}
