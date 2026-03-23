import type { Request, Response } from "express";
import { initDeps } from "../dependencies.js";
import { FirestoreNotificationPreferencesRepository } from "../infrastructure/firestore/FirestoreNotificationPreferencesRepository.js";

export async function getNotificationPreferencesHandler(
  req: Request,
  res: Response
) {
  try {
    const authHeader = req.headers.authorization;
    if (!authHeader?.startsWith("Bearer ")) {
      return res.status(401).json({ error: "Missing token" });
    }

    const idToken = authHeader.substring("Bearer ".length).trim();
    const { auth, firestore } = initDeps();
    const decoded = await auth.verifyIdToken(idToken);
    const preferencesRepo = new FirestoreNotificationPreferencesRepository(firestore);
    const preferences = await preferencesRepo.getOrCreate(decoded.uid);

    return res.status(200).json({
      ok: true,
      preferences,
    });
  } catch (error) {
    console.error("getNotificationPreferencesHandler failed", error);
    return res.status(500).json({ error: "Internal error" });
  }
}
