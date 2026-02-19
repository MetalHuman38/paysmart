import { buildApp } from "./server.js";
import { onRequest } from "firebase-functions/v2/https";
import { APP } from "./config/globals.js";

// Firebase HTTP function export
export const api = onRequest(
  {
    region: APP.region,
    secrets: ["RESEND_API_KEY"],  
    cors: false,
  },
  (req, res) => {
    const app = buildApp();
    return app(req, res);
  }
);
