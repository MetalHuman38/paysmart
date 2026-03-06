import { Request, Response } from "express";
import { initDeps } from "../dependencies.js";
import { authContainer } from "../infrastructure/di/authContainer.js";

export async function passkeyListCredentialsHandler(req: Request, res: Response) {
  try {
    const authHeader = req.headers.authorization;
    if (!authHeader?.startsWith("Bearer ")) {
      return res.status(401).json({ error: "Missing token" });
    }

    const idToken = authHeader.split("Bearer ")[1];
    const { auth } = initDeps();
    const decoded = await auth.verifyIdToken(idToken);

    const { passkeys } = authContainer();
    const result = await passkeys.listCredentials(decoded.uid);

    return res.status(200).json(result);
  } catch (error) {
    console.error("passkeyListCredentialsHandler failed", error);
    return res.status(500).json({ error: "Internal error" });
  }
}
