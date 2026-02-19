import { onCall } from "firebase-functions/v2/https";
import { APP } from "./config/globals.js";

export const echo = onCall(
  {
    region: APP.region,
    enforceAppCheck: true,
  },
  (req) => {
    return { data: req.data, appId: req.app?.appId || null };
  }
);
