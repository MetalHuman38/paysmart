import type { Request, Response } from "express";
import { initDeps } from "../dependencies.js";
import {
  FirestoreNotificationPreferencesRepository,
  type NotificationPreferencesUpdate,
} from "../infrastructure/firestore/FirestoreNotificationPreferencesRepository.js";

export async function updateNotificationPreferencesHandler(
  req: Request,
  res: Response
) {
  try {
    const authHeader = req.headers.authorization;
    if (!authHeader?.startsWith("Bearer ")) {
      return res.status(401).json({ error: "Missing token" });
    }

    const input = parsePreferencesInput(req.body);
    if (!input) {
      return res.status(400).json({ error: "No valid notification preference fields provided" });
    }

    const idToken = authHeader.substring("Bearer ".length).trim();
    const { auth, firestore } = initDeps();
    const decoded = await auth.verifyIdToken(idToken);
    const preferencesRepo = new FirestoreNotificationPreferencesRepository(firestore);
    const preferences = await preferencesRepo.update(decoded.uid, input);

    return res.status(200).json({
      ok: true,
      preferences,
    });
  } catch (error) {
    console.error("updateNotificationPreferencesHandler failed", error);
    return res.status(500).json({ error: "Internal error" });
  }
}

function parsePreferencesInput(body: unknown): NotificationPreferencesUpdate | null {
  if (!body || typeof body !== "object") {
    return null;
  }

  const source = body as Record<string, unknown>;
  const update: NotificationPreferencesUpdate = {};

  if (typeof source.emailTransactional === "boolean") {
    update.emailTransactional = source.emailTransactional;
  }
  if (typeof source.emailProductUpdates === "boolean") {
    update.emailProductUpdates = source.emailProductUpdates;
  }
  if (typeof source.pushTransactional === "boolean") {
    update.pushTransactional = source.pushTransactional;
  }
  if (typeof source.pushProductUpdates === "boolean") {
    update.pushProductUpdates = source.pushProductUpdates;
  }
  if (Object.prototype.hasOwnProperty.call(source, "preferredLocale")) {
    update.preferredLocale =
      typeof source.preferredLocale === "string"
        ? source.preferredLocale
        : null;
  }

  return Object.keys(update).length > 0 ? update : null;
}
