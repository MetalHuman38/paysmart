// src/functions/security.ts
import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { setGlobalOptions } from "firebase-functions/v2/options";
import { onRequest } from "firebase-functions/v2/https";
import { initDeps } from "./dependencies.js";
import { FieldValue } from "firebase-admin/firestore";
import * as admin from "firebase-admin";

setGlobalOptions({ region: "europe-west2" });

const REGION = "europe-west2" as const;

const DEFAULT_ONBOARDING_REQUIRED = {
  set_passcode: true,
  enable_biometrics: true,
  accept_tos: true,
} as const;

/**
 * 1. Seed /users/{uid}/settings/security on first user profile creation
 */
export const seedSecurityOnUserCreate = onDocumentCreated(
  {
    document: "users/{uid}",
    region: REGION,
    memory: "128MiB",
    concurrency: 20,
    cpu: 1,
  },
  async (event) => {
    const { firestore } = initDeps();
    const uid = event.params.uid as string;

    const secRef = firestore
      .collection("users")
      .doc(uid)
      .collection("security")
      .doc("settings");

    await firestore.runTransaction(async (tx) => {
      const secSnap = await tx.get(secRef);

      if (!secSnap.exists) {
        tx.set(secRef, {
          passcodeEnabled: false,
          passwordEnabled: false,
          biometricsRequired: true,
          lockAfterMinutes: 5,
          onboardingRequired: DEFAULT_ONBOARDING_REQUIRED,
          onboardingCompleted: {},
          hasVerifiedEmail: false,
          hasAddedHomeAddress: false,
          hasVerifiedIdentity: false,
          localPassCodeSetAt: null,
          localPasswordSetAt: null,
          updatedAt: FieldValue.serverTimestamp(),
        });
      }
    });
  }
);

/**
 * 2. (Optional) Idempotent HTTPS endpoint to backfill seed data
 */
export const ensureSecurityDoc = onRequest(
  {
    region: REGION,
    cors: false,
    memory: "128MiB",
    concurrency: 20,
    cpu: 1,
  },
  async (req, res) => {
    const { firestore } = initDeps();
    const uid = (req.query.uid as string) ?? "";

    if (!uid) {
      res.status(400).json({ error: "missing uid" });
      return;
    }

    const secRef = firestore
      .collection("users")
      .doc(uid)
      .collection("security")
      .doc("settings");

    await firestore.runTransaction(async (tx) => {
      const snap = await tx.get(secRef);
      if (snap.exists) return;

      tx.set(secRef, {
        passcodeEnabled: false,
        passwordEnabled: false,
        biometricsRequired: false,
        lockAfterMinutes: 5,
        onboardingRequired: DEFAULT_ONBOARDING_REQUIRED,
        onboardingCompleted: {},
        hasVerifiedEmail: false,
        hasAddedHomeAddress: false,
        hasVerifiedIdentity: false,
        localPassCodeSetAt: null,
        localPasswordSetAt: null,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      });
    });

    res.json({ ok: true });
  }
);
