import { FieldValue, Firestore, Timestamp } from "firebase-admin/firestore";

export type ProductUpdateCampaignStatus =
  | "draft"
  | "ready"
  | "processing"
  | "sent"
  | "failed";

export type ProductUpdateCampaign = {
  campaignId: string;
  status: ProductUpdateCampaignStatus;
  title: string;
  subject: string;
  summary: string;
  body?: string;
  area?: string;
  releaseStatus?: string;
  highlights: string[];
  ctaLabel?: string;
  ctaUrl?: string;
  sendPush: boolean;
  sendEmail: boolean;
};

export type CreateProductUpdateCampaignInput = {
  title: string;
  subject?: string;
  summary: string;
  body?: string;
  area?: string;
  releaseStatus?: string;
  highlights?: string[];
  ctaLabel?: string;
  ctaUrl?: string;
  status?: ProductUpdateCampaignStatus;
  sendPush?: boolean;
  sendEmail?: boolean;
  createdByUid?: string | null;
  createdByEmail?: string | null;
};

export type ProductUpdateCampaignDeliverySnapshot = {
  emailSentAtMs?: number;
  notificationSentAtMs?: number;
};

export type ProductUpdateCampaignDeliveryStats = {
  inboxRecipients: number;
  pushEligibleInstallations: number;
  pushDelivered: number;
  pushFailed: number;
  emailRecipients: number;
  emailDelivered: number;
  emailFailed: number;
};

export type PublishedProductUpdateCampaign = ProductUpdateCampaign & {
  sentAtMs: number;
};

export class FirestoreProductUpdateCampaignRepository {
  constructor(private readonly firestore: Firestore) {}

  private collection() {
    return this.firestore.collection("notificationCampaigns");
  }

  private ref(campaignId: string) {
    return this.collection().doc(campaignId);
  }

  private deliveryRef(campaignId: string, uid: string) {
    return this.ref(campaignId).collection("deliveries").doc(uid);
  }

  async createCampaign(input: CreateProductUpdateCampaignInput): Promise<string> {
    const docRef = this.collection().doc();
    await docRef.set({
      campaignType: "product_update",
      status: input.status ?? "draft",
      title: input.title.trim(),
      subject: input.subject?.trim() || input.title.trim(),
      summary: input.summary.trim(),
      body: input.body?.trim() || null,
      area: input.area?.trim() || null,
      releaseStatus: input.releaseStatus?.trim() || null,
      highlights: (input.highlights || []).map((item) => item.trim()).filter(Boolean),
      ctaLabel: input.ctaLabel?.trim() || null,
      ctaUrl: input.ctaUrl?.trim() || null,
      sendPush: input.sendPush !== false,
      sendEmail: input.sendEmail !== false,
      createdByUid: input.createdByUid?.trim() || null,
      createdByEmail: input.createdByEmail?.trim().toLowerCase() || null,
      createdAt: FieldValue.serverTimestamp(),
      updatedAt: FieldValue.serverTimestamp(),
    });
    return docRef.id;
  }

  async dispatchCampaign(campaignId: string): Promise<void> {
    await this.ref(campaignId).set(
      {
        status: "ready",
        updatedAt: FieldValue.serverTimestamp(),
      },
      { merge: true }
    );
  }

  async claimReadyCampaign(campaignId: string): Promise<ProductUpdateCampaign | null> {
    const ref = this.ref(campaignId);
    return this.firestore.runTransaction(async (tx) => {
      const snap = await tx.get(ref);
      if (!snap.exists) {
        return null;
      }

      const data = snap.data() as Record<string, unknown>;
      if ((data.campaignType as string | undefined) !== "product_update") {
        return null;
      }
      if ((data.status as string | undefined) !== "ready") {
        return null;
      }

      tx.set(
        ref,
        {
          status: "processing",
          processingStartedAt: FieldValue.serverTimestamp(),
          updatedAt: FieldValue.serverTimestamp(),
          lastError: FieldValue.delete(),
        },
        { merge: true }
      );

      return mapCampaign(ref.id, data);
    });
  }

  async markSent(
    campaignId: string,
    stats: ProductUpdateCampaignDeliveryStats
  ): Promise<void> {
    await this.ref(campaignId).set(
      {
        status: "sent",
        sentAt: FieldValue.serverTimestamp(),
        updatedAt: FieldValue.serverTimestamp(),
        deliveryStats: stats,
      },
      { merge: true }
    );
  }

  async markFailed(
    campaignId: string,
    errorMessage: string,
    stats?: Partial<ProductUpdateCampaignDeliveryStats>
  ): Promise<void> {
    await this.ref(campaignId).set(
      {
        status: "failed",
        updatedAt: FieldValue.serverTimestamp(),
        lastError: errorMessage.slice(0, 512),
        ...(stats ? { deliveryStats: stats } : {}),
      },
      { merge: true }
    );
  }

  async getDeliverySnapshot(
    campaignId: string,
    uid: string
  ): Promise<ProductUpdateCampaignDeliverySnapshot> {
    const snap = await this.deliveryRef(campaignId, uid).get();
    if (!snap.exists) {
      return {};
    }
    const data = snap.data() as Record<string, unknown>;
    return {
      emailSentAtMs: timestampToMillis(data.emailSentAt),
      notificationSentAtMs: timestampToMillis(data.notificationSentAt),
    };
  }

  async markNotificationDelivered(
    campaignId: string,
    uid: string,
    notificationId: string
  ): Promise<void> {
    await this.deliveryRef(campaignId, uid).set(
      {
        notificationId,
        notificationSentAt: FieldValue.serverTimestamp(),
        updatedAt: FieldValue.serverTimestamp(),
      },
      { merge: true }
    );
  }

  async markNotificationFailed(
    campaignId: string,
    uid: string,
    errorMessage: string
  ): Promise<void> {
    await this.deliveryRef(campaignId, uid).set(
      {
        notificationFailedAt: FieldValue.serverTimestamp(),
        notificationLastError: errorMessage.slice(0, 512),
        updatedAt: FieldValue.serverTimestamp(),
      },
      { merge: true }
    );
  }

  async markEmailDelivered(campaignId: string, uid: string, email: string): Promise<void> {
    await this.deliveryRef(campaignId, uid).set(
      {
        email,
        emailSentAt: FieldValue.serverTimestamp(),
        updatedAt: FieldValue.serverTimestamp(),
      },
      { merge: true }
    );
  }

  async markEmailFailed(campaignId: string, uid: string, errorMessage: string): Promise<void> {
    await this.deliveryRef(campaignId, uid).set(
      {
        emailFailedAt: FieldValue.serverTimestamp(),
        emailLastError: errorMessage.slice(0, 512),
        updatedAt: FieldValue.serverTimestamp(),
      },
      { merge: true }
    );
  }

  async listPublishedCampaigns(limit = 20): Promise<PublishedProductUpdateCampaign[]> {
    const normalizedLimit = normalizeLimit(limit);
    const rawLimit = Math.max(normalizedLimit * 5, 50);
    const snap = await this.collection().orderBy("sentAt", "desc").limit(rawLimit).get();

    return snap.docs
      .map((doc) => mapPublishedCampaign(doc.id, doc.data() as Record<string, unknown>))
      .filter((campaign): campaign is PublishedProductUpdateCampaign => campaign !== null)
      .slice(0, normalizedLimit);
  }
}

function mapCampaign(
  campaignId: string,
  data: Record<string, unknown>
): ProductUpdateCampaign {
  return {
    campaignId,
    status: normalizeStatus(data.status),
    title: asString(data.title),
    subject: asString(data.subject) || asString(data.title),
    summary: asString(data.summary),
    body: asOptionalString(data.body),
    area: asOptionalString(data.area),
    releaseStatus: asOptionalString(data.releaseStatus),
    highlights: Array.isArray(data.highlights)
      ? data.highlights.map((item) => asString(item)).filter(Boolean)
      : [],
    ctaLabel: asOptionalString(data.ctaLabel),
    ctaUrl: asOptionalString(data.ctaUrl),
    sendPush: data.sendPush !== false,
    sendEmail: data.sendEmail !== false,
  };
}

function mapPublishedCampaign(
  campaignId: string,
  data: Record<string, unknown>
): PublishedProductUpdateCampaign | null {
  if (asString(data.campaignType) !== "product_update") {
    return null;
  }
  if (asString(data.status) !== "sent") {
    return null;
  }

  const sentAtMs = timestampToMillis(data.sentAt);
  if (typeof sentAtMs !== "number" || !Number.isFinite(sentAtMs)) {
    return null;
  }

  return {
    ...mapCampaign(campaignId, data),
    status: "sent",
    sentAtMs,
  };
}

function normalizeStatus(raw: unknown): ProductUpdateCampaignStatus {
  switch (asString(raw)) {
    case "draft":
    case "ready":
    case "processing":
    case "sent":
    case "failed":
      return asString(raw) as ProductUpdateCampaignStatus;
    default:
      return "draft";
  }
}

function asString(value: unknown): string {
  return typeof value === "string" ? value.trim() : "";
}

function asOptionalString(value: unknown): string | undefined {
  const normalized = asString(value);
  return normalized || undefined;
}

function timestampToMillis(value: unknown): number | undefined {
  return value instanceof Timestamp ? value.toMillis() : undefined;
}

function normalizeLimit(value: number): number {
  if (!Number.isFinite(value)) {
    return 20;
  }
  return Math.max(1, Math.min(Math.trunc(value), 50));
}
