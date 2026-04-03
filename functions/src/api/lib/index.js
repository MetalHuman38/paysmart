import { buildApp } from "./server.js";
import { onRequest } from "firebase-functions/v2/https";
import { APP } from "./config/globals.js";
import { API_FUNCTION_SECRETS } from "./config/functionSecrets.js";
import { processIdentityUploadReview } from "./workers/processIdentityUploadReview.js";
import { processIdentityReviewNotifications, processSecuritySettingNotifications, processWalletTransactionNotifications, } from "./workers/processTransactionalNotifications.js";
import { processProductUpdateCampaign } from "./workers/processProductUpdateCampaign.js";
const apiApp = buildApp();
// Firebase HTTP function export
export const api = onRequest({
    region: APP.region,
    secrets: [...API_FUNCTION_SECRETS],
    cors: false,
}, apiApp);
export { processIdentityUploadReview, processIdentityReviewNotifications, processWalletTransactionNotifications, processSecuritySettingNotifications, processProductUpdateCampaign, };
//# sourceMappingURL=index.js.map