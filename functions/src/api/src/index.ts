import { buildApp } from "./server.js";
import { onRequest } from "firebase-functions/v2/https";
import { APP } from "./config/globals.js";
import { processIdentityUploadReview } from "./workers/processIdentityUploadReview.js";

// Firebase HTTP function export
export const api = onRequest(
  {
    region: APP.region,
    secrets: [
      "RESEND_API_KEY",
      "ADDRESS_VALIDATION_API_KEY",
      "EXCHANGE_RATE_API_KEY",
      "STRIPE_SECRET_KEY",
      "STRIPE_PUBLISHABLE_KEY",
      "STRIPE_WEBHOOK_SECRET",
      "FLUTTERWAVE_PUBLIC_KEY",
      "FLUTTERWAVE_WEBHOOK_SECRET_HASH",
      "FLUTTER_WAVE_CLIENT_ID",
      "FLUTTER_WAVE_CLIENT_SECRET"
    ],
    cors: false,
  },
  (req, res) => {
    const app = buildApp();
    return app(req, res);
  }
);

export { processIdentityUploadReview };
