import { onDocumentWritten } from "firebase-functions/v2/firestore";
import { APP } from "../config/globals.js";
import { initDeps } from "../dependencies.js";
import { notificationContainer } from "../infrastructure/di/notificationContainer.js";
import { FirestoreNotificationPreferencesRepository } from "../infrastructure/firestore/FirestoreNotificationPreferencesRepository.js";
import { FirestoreProductUpdateCampaignRepository } from "../infrastructure/firestore/FirestoreProductUpdateCampaignRepository.js";
import { FirestoreUserRepository } from "../infrastructure/firestore/FirestoreUserRepository.js";
import { EmailSubscriptionService } from "../services/emailSubscriptionService.js";
import { ProductUpdateCampaignService } from "../services/productUpdateCampaignService.js";

type ProductUpdateCampaignStatusDoc = {
  status?: string | null;
  campaignType?: string | null;
};

export const processProductUpdateCampaign = onDocumentWritten(
  {
    region: APP.region,
    document: "notificationCampaigns/{campaignId}",
    retry: false,
    timeoutSeconds: 540,
    memory: "512MiB",
  },
  async (event) => {
    const before = event.data?.before.data() as
      | ProductUpdateCampaignStatusDoc
      | undefined;
    const after = event.data?.after.data() as
      | ProductUpdateCampaignStatusDoc
      | undefined;

    if (!after) {
      return;
    }
    if ((after.campaignType || "").trim() !== "product_update") {
      return;
    }
    if ((after.status || "").trim() !== "ready") {
      return;
    }
    if ((before?.status || "").trim() === "ready") {
      return;
    }

    const { firestore, auth, getConfig } = initDeps();
    const config = getConfig();
    const { notificationDelivery } = notificationContainer();
    const service = new ProductUpdateCampaignService(
      new FirestoreProductUpdateCampaignRepository(firestore),
      new FirestoreNotificationPreferencesRepository(firestore),
      notificationDelivery,
      config.getMailer(),
      new EmailSubscriptionService(
        config.getEmailUnsubscribeSecret(),
        new URL("/email/unsubscribe", config.getPublicSiteOrigin()).toString()
      ),
      new FirestoreUserRepository(firestore),
      auth,
      config.getProductUpdatesUrl()
    );

    await service.processReadyCampaign(event.params.campaignId as string);
  }
);
