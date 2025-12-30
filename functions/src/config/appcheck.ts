// C:\Users\Metal\voltpay\functions\src\lib\middleware\appCheck.ts
import type { NextFunction, Request, Response } from "express";
import * as admin from "firebase-admin";
import { APP, SECURITY } from "./globals";

export interface AppCheckClaims {
  sub: string;
  app_id?: string;
  [key: string]: any;
}

// Augment Express Request type to include appCheck property
declare module "express-serve-static-core" {
  interface Request {
    appCheck?: AppCheckClaims | null;
  }
}

// Middleware to require and verify Firebase App Check token
export async function requireAppCheck(req: Request, res: Response, next: NextFunction) {
  // Emulator or disabled → pass through (with a marker)
  if (APP.emulator || !SECURITY.appCheckRequired) {
    req.appCheck = null;
    return next();
  }

  try {
    const token =
      (req.header("X-Firebase-AppCheck") ||
        req.header("X-Firebase-AppCheck-Token") || // older alt header
        "").trim();

    if (!token) {
      return res.status(401).json({ error: "Missing App Check token" });
    }

    const { appCheck } = admin;
    const result = await appCheck().verifyToken(token);
    // attach claims for downstream handlers (optional)
    req.appCheck = result.token as unknown as AppCheckClaims;
    return next();
  } catch (err) {
    return res.status(401).json({ error: "Invalid App Check token" });
  }
}
