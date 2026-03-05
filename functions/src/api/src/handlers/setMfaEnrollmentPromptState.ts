import { Request, Response } from "express";
import { initDeps } from "../dependencies.js";
import { authContainer } from "../infrastructure/di/authContainer.js";
import { SetMfaEnrollmentPromptState } from "../application/usecase/SetMfaEnrollmentPromptState.js";

export async function setMfaEnrollmentPromptStateHandler(req: Request, res: Response) {
  try {
    const authHeader = req.headers.authorization;
    if (!authHeader?.startsWith("Bearer ")) {
      return res.status(401).json({ error: "Missing token" });
    }

    const hasSkippedMfaEnrollmentPrompt = req.body?.hasSkippedMfaEnrollmentPrompt;
    if (typeof hasSkippedMfaEnrollmentPrompt !== "boolean") {
      return res.status(400).json({
        error: "hasSkippedMfaEnrollmentPrompt must be a boolean",
      });
    }

    const idToken = authHeader.split("Bearer ")[1];
    const { auth } = initDeps();
    const decoded = await auth.verifyIdToken(idToken);

    const { securitySettings } = authContainer();
    const useCase = new SetMfaEnrollmentPromptState(securitySettings);
    await useCase.execute(decoded.uid, hasSkippedMfaEnrollmentPrompt);

    return res.status(200).json({
      ok: true,
      hasSkippedMfaEnrollmentPrompt,
    });
  } catch (error) {
    console.error(error);
    return res.status(500).json({ error: "Internal error" });
  }
}
