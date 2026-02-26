import { Request, Response } from "express";
import { initDeps } from "../dependencies.js";
import { authContainer } from "../infrastructure/di/authContainer.js";
import { ResumeIdentityProviderSession } from "../application/usecase/ResumeIdentityProviderSession.js";

export async function identityProviderResumeHandler(req: Request, res: Response) {
  try {
    const authHeader = req.headers.authorization;
    if (!authHeader?.startsWith("Bearer ")) {
      return res.status(401).json({ error: "Missing token" });
    }

    const sessionId = (req.body?.sessionId ?? "").toString().trim();
    if (!sessionId) {
      return res.status(400).json({ error: "Missing sessionId" });
    }

    const idToken = authHeader.split("Bearer ")[1];
    const { auth } = initDeps();
    const decoded = await auth.verifyIdToken(idToken);

    const { identityProvider } = authContainer();
    const useCase = new ResumeIdentityProviderSession(identityProvider);
    const session = await useCase.execute(decoded.uid, { sessionId });

    return res.status(200).json(session);
  } catch (error) {
    const message = error instanceof Error ? error.message : "Internal error";
    if (message.includes("not found")) {
      return res.status(404).json({ error: message });
    }
    if (message.includes("Missing") || message.includes("Invalid")) {
      return res.status(400).json({ error: message });
    }

    console.error("identityProviderResumeHandler failed", error);
    return res.status(500).json({ error: "Internal error" });
  }
}
