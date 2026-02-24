import { defineSecret, defineString } from "firebase-functions/params";
import * as dotenv from "dotenv";
dotenv.config();
// Secret — must be stored in Secret Manager
// 🔐 Secrets (Secret Manager)
export const RESEND_API_KEY = defineSecret("RESEND_API_KEY");
export const ADDRESS_VALIDATION_API_KEY = defineSecret("ADDRESS_VALIDATION_API_KEY");
export const EXCHANGE_RATE_API_KEY = defineSecret("EXCHANGE_RATE_API_KEY");
// 🟢 Non-secret parameters
export const MAIL_FROM = defineString("MAIL_FROM", {
    default: "PaySmart <no-reply@metalbrain.net>",
});
export const VERIFY_URL = defineString("VERIFY_URL", {
    default: "https://pay-smart.net/verify",
});
export const SEND_REAL_EMAILS = defineString("SEND_REAL_EMAILS", {
    default: "false",
});
export const FACEBOOK_APP_ID = defineString("FACEBOOK_APP_ID", {
    default: process.env.FACEBOOK_APP_ID || "",
});
export const FACEBOOK_APP_SECRET = defineSecret("FACEBOOK_APP_SECRET");
export const IDENTITY_KMS_KEY_NAME = defineString("IDENTITY_UPLOAD_KMS_KEY_NAME", {
    default: process.env.IDENTITY_UPLOAD_KMS_KEY_NAME || "",
});
export const IDENTITY_MAX_PAYLOAD_BYTES = defineString("IDENTITY_UPLOAD_MAX_PAYLOAD_BYTES", {
    default: process.env.IDENTITY_UPLOAD_MAX_PAYLOAD_BYTES || "15728640", // 15 MiB
});
export const STRIPE_SECRET_KEY = defineSecret("STRIPE_SECRET_KEY");
export const STRIPE_WEBHOOK_SECRET = defineSecret("STRIPE_WEBHOOK_SECRET");
export const STRIPE_API_KEY = defineSecret("STRIPE_API_KEY");
//# sourceMappingURL=params.js.map