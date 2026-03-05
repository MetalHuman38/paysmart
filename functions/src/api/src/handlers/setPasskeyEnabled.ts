import { Request, Response } from "express";
import { initDeps } from "../dependencies.js";
import { authContainer } from "../infrastructure/di/authContainer.js";
import { SetPasskeyEnabled } from "../application/usecase/SetPasskeyEnabled.js";

export async function setPasskeyEnabledHandler(req: Request, res: Response) {
  try {
    const authHeader = req.headers.authorization;
    if (!authHeader?.startsWith("Bearer ")) {
      return res.status(401).json({ error: "Missing token" });
    }

    const passkeyEnabled = req.body?.passkeyEnabled;
    if (typeof passkeyEnabled !== "boolean") {
      return res.status(400).json({
        error: "passkeyEnabled must be a boolean",
      });
    }

    const idToken = authHeader.split("Bearer ")[1];
    const { auth } = initDeps();
    const decoded = await auth.verifyIdToken(idToken);

    const { securitySettings } = authContainer();
    const useCase = new SetPasskeyEnabled(securitySettings);
    await useCase.execute(decoded.uid, passkeyEnabled);

    return res.status(200).json({
      ok: true,
      passkeyEnabled,
      hasSkippedPasskeyEnrollmentPrompt: !passkeyEnabled,
    });
  } catch (error) {
    console.error(error);
    return res.status(500).json({ error: "Internal error" });
  }
}
