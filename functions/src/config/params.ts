import { defineSecret, defineString } from "firebase-functions/params";
import * as dotenv from "dotenv";

dotenv.config();

// Secret — must be stored in Secret Manager
// 🔐 Secrets (Secret Manager)
export const RESEND_API_KEY = defineSecret("RESEND_API_KEY");

// 🟢 Non-secret parameters
export const MAIL_FROM = defineString("MAIL_FROM", {
  default: "PaySmart <no-reply@metalbrain.net>",
});

export const VERIFY_URL = defineString("VERIFY_URL", {
  default: "https://metalbrain.net/verify",
});

export const SEND_REAL_EMAILS = defineString("SEND_REAL_EMAILS", {
  default: "false",
});

export const FACEBOOK_APP_ID = defineString("FACEBOOK_APP_ID", {
  default: process.env.FACEBOOK_APP_ID || "",
});

export const FACEBOOK_APP_SECRET = defineSecret("FACEBOOK_APP_SECRET");
