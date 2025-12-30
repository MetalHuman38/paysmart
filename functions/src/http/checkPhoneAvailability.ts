import { Request, Response } from "express";
import { getAuth } from "firebase-admin/auth";
import { corsify } from "../utils";

// only E.164 phone numbers to match Firebase Auth expectations
const E164_REGEX = /^\+[1-9]\d{6,14}$/;

export async function checkPhoneAvailability(req: Request, res: Response) {
  corsify(res);

  const rawPhone =
    (req.body?.phoneNumber ?? req.query?.phoneNumber ?? "").toString().trim();
  const phone = rawPhone.replace(/[^\d+]/g, "");

  if (!phone) {
    return res.status(400).json({
      error: {
        code: "invalid-argument",
        message: "Missing phone number",
      },
    });
  }

  if (!E164_REGEX.test(phone)) {
    return res.status(400).json({
      error: {
        code: "invalid-argument",
        message: "Phone number must be in E.164 format (e.g. +1234567890)",
      },
    });
  }

  try {
    await getAuth().getUserByPhoneNumber(phone);
    return res.status(409).json({
      available: false,
      error: {
        code: "already-exists",
        message: "Phone number is already registered",
      },
    });
  } catch (err: any) {
    if (err?.code === "auth/user-not-found") {
      return res.status(200).json({ available: true });
    }

    console.error("checkPhoneAvailability error:", err);
    return res.status(500).json({
      error: {
        code: "internal",
        message: "Unable to check phone availability",
      },
    });
  }
}
