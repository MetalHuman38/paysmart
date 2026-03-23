import { buildApp } from "./server.js";
import { onRequest } from "firebase-functions/v2/https";
import { APP } from "./config/globals.js";
import { API_FUNCTION_SECRETS } from "./config/functionSecrets.js";
import { processIdentityUploadReview } from "./workers/processIdentityUploadReview.js";
import {
  processIdentityReviewNotifications,
  processSecuritySettingNotifications,
  processWalletTransactionNotifications,
} from "./workers/processTransactionalNotifications.js";
import { processProductUpdateCampaign } from "./workers/processProductUpdateCampaign.js";

// Firebase HTTP function export
export const api = onRequest(
  {
    region: APP.region,
    secrets: [...API_FUNCTION_SECRETS],
    cors: false,
  },
  (req, res) => {
    const app = buildApp();
    return app(req, res);
  }
);

export {
  processIdentityUploadReview,
  processIdentityReviewNotifications,
  processWalletTransactionNotifications,
  processSecuritySettingNotifications,
  processProductUpdateCampaign,
};
