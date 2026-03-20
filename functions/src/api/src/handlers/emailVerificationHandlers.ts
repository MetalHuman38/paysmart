import { Request, Response } from "express";
import { apiContainer } from "../infrastructure/di/apiContainer.js";

export async function generateEmailVerificationHandler(
  req: Request,
  res: Response
) {
  try {
    const { getUIDFromAuthHeader } = apiContainer();
    const uid = await getUIDFromAuthHeader.execute(
      req.headers.authorization
    );
    const email = String(req.body?.email || "").trim().toLowerCase();
    if (!email) {
      return res.status(400).json({ error: "Email is required" });
    }

    const { generateEmailVerification } = apiContainer();
    const result = await generateEmailVerification.execute({ uid, email });

    if (!result.sent) {
      if (result.retryAfter) {
        res.setHeader("Retry-After", String(result.retryAfter));
      }

      if (result.reason === "already_verified") {
        return res.status(409).json({
          error: "Email is already verified",
          code: "email_already_verified",
          sent: false,
        });
      }

      if (result.reason === "daily_limit") {
        return res.status(429).json({
          error: "Daily email verification limit reached",
          code: "email_verification_daily_limit",
          retryAfter: result.retryAfter ?? null,
          sent: false,
        });
      }

      return res.status(429).json({
        error: "Email verification cooldown active",
        code: "email_verification_cooldown",
        retryAfter: result.retryAfter ?? null,
        sent: false,
      });
    }

    return res.json({ sent: true });
  } catch (err: any) {
    console.error("email verification error:", err);
    return res.status(500).json({ error: err.message });
  }
}

export async function checkEmailVerificationStatusHandler(
  req: Request,
  res: Response
) {
  try {
    const { getUIDFromAuthHeader } = apiContainer();
    const { checkEmailVerificationStatus } = apiContainer();
    const uid = await getUIDFromAuthHeader.execute(
      req.headers.authorization
    );
    return res.json(await checkEmailVerificationStatus.execute(uid));
  } catch (err: any) {
    console.error("checkEmailVerificationStatus error:", err);
    return res.status(500).json({ error: err.message });
  }
}
