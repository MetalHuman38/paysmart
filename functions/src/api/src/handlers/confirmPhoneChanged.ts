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
    const { authService, securitySettings, userRepo } = authContainer();

    const useCase = new ConfirmPhoneChanged(authService, securitySettings, userRepo);
    await useCase.execute(decoded.uid, phoneNumber);
    return res.status(200).json({ ok: true });
  } catch (e) {
    if (
      e instanceof Error &&
      (
        e.message === "Invalid phone number format" ||
        e.message === "Verified phone number is unavailable" ||
        e.message === "Verified phone number does not match requested phone number"
      )
    ) {
      const statusCode =
        e.message === "Verified phone number does not match requested phone number" ? 409 : 400;
      return res.status(statusCode).json({ error: e.message });
    }

    console.error("confirmPhoneChanged failed", e);
    return res.status(500).json({ error: "Internal error" });
  }
}
