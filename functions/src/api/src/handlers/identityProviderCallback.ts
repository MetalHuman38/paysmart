import { Request, Response } from "express";
import { initDeps } from "../dependencies.js";
import { authContainer } from "../infrastructure/di/authContainer.js";
import { SubmitIdentityProviderCallback } from "../application/usecase/SubmitIdentityProviderCallback.js";

export async function identityProviderCallbackHandler(
  req: Request,
  res: Response
) {
  try {
    const authHeader = req.headers.authorization;
    if (!authHeader?.startsWith("Bearer ")) {
      return res.status(401).json({ error: "Missing token" });
    }

    const event = (req.body?.event ?? "").toString().trim();
    if (!event) {
      return res.status(400).json({ error: "Missing event" });
    }

    const sessionId = asOptionalString(req.body?.sessionId);
    const providerRef = asOptionalString(req.body?.providerRef);
    const rawDeepLink = asOptionalString(req.body?.rawDeepLink);

    const idToken = authHeader.split("Bearer ")[1];
    const { auth } = initDeps();
    const decoded = await auth.verifyIdToken(idToken);

    const { identityProvider, securitySettings } = authContainer();
    const useCase = new SubmitIdentityProviderCallback(
      identityProvider,
      securitySettings
    );
    const result = await useCase.execute(decoded.uid, {
      event,
      sessionId,
      providerRef,
      rawDeepLink,
    });

    return res.status(200).json(result);
  } catch (error) {
    const message = error instanceof Error ? error.message : "Internal error";
    if (message.includes("not found")) {
      return res.status(404).json({ error: message });
    }
    if (message.includes("Missing") || message.includes("Invalid")) {
      return res.status(400).json({ error: message });
    }

    console.error("identityProviderCallbackHandler failed", error);
    return res.status(500).json({ error: "Internal error" });
  }
}

function asOptionalString(value: unknown): string | undefined {
  if (typeof value !== "string") return undefined;
  const trimmed = value.trim();
  return trimmed || undefined;
}
