import type { Request, Response } from "express";
import { initDeps } from "../dependencies.js";
import { notificationContainer } from "../infrastructure/di/notificationContainer.js";
import { FirestoreNotificationPreferencesRepository } from "../infrastructure/firestore/FirestoreNotificationPreferencesRepository.js";
import { FirestoreProductUpdateCampaignRepository } from "../infrastructure/firestore/FirestoreProductUpdateCampaignRepository.js";
import { FirestoreUserRepository } from "../infrastructure/firestore/FirestoreUserRepository.js";
import { EmailSubscriptionService } from "../services/emailSubscriptionService.js";
import { ProductUpdateCampaignService } from "../services/productUpdateCampaignService.js";

export async function adminCreateProductUpdateCampaignHandler(
  req: Request,
  res: Response
) {
  try {
    const { firestore, auth, getConfig } = initDeps();
    const config = getConfig();
    const { notificationDelivery } = notificationContainer();
    const admin = (res.locals as Record<string, unknown>).admin as
      | { uid: string; email: string | null }
      | undefined;

    const campaignService = new ProductUpdateCampaignService(
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

    const campaignId = await campaignService.createCampaign({
      title: asRequiredString(req.body?.title, "title"),
      subject: asOptionalString(req.body?.subject),
      summary: asRequiredString(req.body?.summary, "summary"),
      body: asOptionalString(req.body?.body),
      area: asOptionalString(req.body?.area),
      releaseStatus: asOptionalString(req.body?.releaseStatus),
      highlights: asStringArray(req.body?.highlights),
      ctaLabel: asOptionalString(req.body?.ctaLabel),
      ctaUrl: asOptionalString(req.body?.ctaUrl),
      status: req.body?.dispatch === true ? "ready" : "draft",
      sendPush: req.body?.sendPush !== false,
      sendEmail: req.body?.sendEmail !== false,
      createdByUid: admin?.uid,
      createdByEmail: admin?.email,
    });

    return res.status(201).json({
      ok: true,
      campaignId,
      status: req.body?.dispatch === true ? "ready" : "draft",
    });
  } catch (error) {
    const message = error instanceof Error ? error.message : "Internal error";
    if (message.includes("required")) {
      return res.status(400).json({ error: message });
    }
    console.error("adminCreateProductUpdateCampaignHandler failed", error);
    return res.status(500).json({ error: "Internal error" });
  }
}

function asRequiredString(value: unknown, field: string): string {
  if (typeof value !== "string" || !value.trim()) {
    throw new Error(`${field} is required`);
  }
  return value.trim();
}

function asOptionalString(value: unknown): string | undefined {
  return typeof value === "string" && value.trim() ? value.trim() : undefined;
}

function asStringArray(value: unknown): string[] {
  if (!Array.isArray(value)) {
    return [];
  }
  return value
    .map((item) => (typeof item === "string" ? item.trim() : ""))
    .filter(Boolean);
}
