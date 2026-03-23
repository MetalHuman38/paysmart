import { FieldValue, Firestore } from "firebase-admin/firestore";

export type NotificationInstallationRegistration = {
  installationId: string;
  fcmToken: string;
  platform: "android";
  locale?: string;
  appVersion?: string;
  notificationsPermissionGranted: boolean;
  appCheckAppId?: string | null;
};

export type NotificationInboxWrite = {
  notificationId?: string;
  type: string;
  channel: string;
  title: string;
  body: string;
  deepLink?: string | null;
  metadata?: Record<string, unknown>;
};

export type NotificationPushInstallation = {
  installationId: string;
  fcmToken: string;
  platform: "android";
  locale?: string;
  appVersion?: string;
};

export class FirestoreNotificationRepository {
  constructor(private readonly firestore: Firestore) {}

  private notificationCollection(uid: string) {
    return this.firestore.collection("users").doc(uid).collection("notifications");
  }

  private installationRef(uid: string, installationId: string) {
    return this.firestore
      .collection("users")
      .doc(uid)
      .collection("installations")
      .doc(installationId);
  }

  private installationCollection(uid: string) {
    return this.firestore.collection("users").doc(uid).collection("installations");
  }

  async registerInstallation(
    uid: string,
    input: NotificationInstallationRegistration
  ): Promise<void> {
    const installationId = input.installationId.trim();
    const fcmToken = input.fcmToken.trim();
    if (!uid.trim()) throw new Error("Missing uid");
    if (!installationId) throw new Error("Missing installationId");
    if (!fcmToken) throw new Error("Missing fcmToken");

    const duplicates = await this.firestore
      .collectionGroup("installations")
      .where("installationId", "==", installationId)
      .get();

    const cleanupBatch = this.firestore.batch();
    duplicates.docs.forEach((doc) => {
      const ownerUid = doc.ref.parent.parent?.id ?? "";
      if (ownerUid && ownerUid !== uid) {
        cleanupBatch.delete(doc.ref);
      }
    });
    await cleanupBatch.commit();

    const ref = this.installationRef(uid, installationId);
    const existing = await ref.get();
    const now = FieldValue.serverTimestamp();

    await ref.set(
      {
        installationId,
        uid,
        platform: input.platform,
        fcmToken,
        locale: input.locale?.trim() || FieldValue.delete(),
        appVersion: input.appVersion?.trim() || FieldValue.delete(),
        notificationsPermissionGranted: input.notificationsPermissionGranted,
        appCheckAppId: input.appCheckAppId?.trim() || FieldValue.delete(),
        lastRegisteredAt: now,
        lastSeenAt: now,
        updatedAt: now,
        createdAt: existing.exists ? existing.get("createdAt") ?? now : now,
      },
      { merge: true }
    );
  }

  async createInboxNotification(uid: string, input: NotificationInboxWrite): Promise<string> {
    const cleanUid = uid.trim();
    if (!cleanUid) throw new Error("Missing uid");

    const title = input.title.trim();
    const body = input.body.trim();
    if (!title) throw new Error("Missing notification title");
    if (!body) throw new Error("Missing notification body");

    const collection = this.notificationCollection(cleanUid);
    const docRef = input.notificationId?.trim() ?
      collection.doc(input.notificationId.trim()) :
      collection.doc();
    const existing = await docRef.get();

    const now = FieldValue.serverTimestamp();
    await docRef.set(
      {
        notificationId: docRef.id,
        type: input.type.trim() || "general",
        channel: input.channel.trim() || "account_updates",
        title,
        body,
        deepLink: input.deepLink?.trim() || null,
        metadata: input.metadata ?? {},
        createdAt: existing.exists ? existing.get("createdAt") ?? now : now,
        updatedAt: now,
        readAt: existing.exists ? existing.get("readAt") ?? null : null,
      },
      { merge: true }
    );

    return docRef.id;
  }

  async listPushInstallations(uid: string): Promise<NotificationPushInstallation[]> {
    const cleanUid = uid.trim();
    if (!cleanUid) throw new Error("Missing uid");

    const snap = await this.installationCollection(cleanUid)
      .where("notificationsPermissionGranted", "==", true)
      .get();

    const installations: NotificationPushInstallation[] = [];
    snap.docs.forEach((doc) => {
      const data = doc.data() as Record<string, unknown>;
      const fcmToken = asString(data.fcmToken);
      if (!fcmToken) {
        return;
      }

      const installation: NotificationPushInstallation = {
        installationId: doc.id,
        fcmToken,
        platform: "android",
      };
      const locale = asOptionalString(data.locale);
      const appVersion = asOptionalString(data.appVersion);
      if (locale) {
        installation.locale = locale;
      }
      if (appVersion) {
        installation.appVersion = appVersion;
      }
      installations.push(installation);
    });

    return installations;
  }

  async deleteInstallations(uid: string, installationIds: string[]): Promise<void> {
    const cleanUid = uid.trim();
    if (!cleanUid || installationIds.length === 0) {
      return;
    }

    const uniqueIds = Array.from(
      new Set(installationIds.map((value) => value.trim()).filter(Boolean))
    );
    if (uniqueIds.length === 0) {
      return;
    }

    const batch = this.firestore.batch();
    uniqueIds.forEach((installationId) => {
      batch.delete(this.installationRef(cleanUid, installationId));
    });
    await batch.commit();
  }
}

function asString(value: unknown): string {
  return typeof value === "string" ? value.trim() : "";
}

function asOptionalString(value: unknown): string | undefined {
  const normalized = asString(value);
  return normalized || undefined;
}
