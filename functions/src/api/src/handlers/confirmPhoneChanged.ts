import { Request, Response } from "express";
import { initDeps } from "../dependencies.js";
import { authContainer } from "../infrastructure/di/authContainer.js";
import { ConfirmPhoneChanged } from "../application/usecase/ConfirmPhoneChanged.js";

export async function confirmPhoneChangedHandler(req: Request, res: Response) {
  try {
    const authHeader = req.headers.authorization;
    if (!authHeader?.startsWith("Bearer ")) {
      return res.status(401).json({ error: "Missing token" });
    }

    const phoneNumber = (req.body?.phoneNumber ?? "").toString().trim();
    if (!phoneNumber) {
      return res.status(400).json({ error: "Missing phoneNumber" });
    }

    const idToken = authHeader.split("Bearer ")[1];
    const { auth } = initDeps();
    const decoded = await auth.verifyIdToken(idToken);
    const { securitySettings, userRepo } = authContainer();

    const useCase = new ConfirmPhoneChanged(securitySettings, userRepo);
    await useCase.execute(decoded.uid, phoneNumber);
    return res.status(200).json({ ok: true });
  } catch (e) {
    console.error("confirmPhoneChanged failed", e);
    return res.status(500).json({ error: "Internal error" });
  }
}
