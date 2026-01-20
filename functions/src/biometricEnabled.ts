// src/handlers/setPasswordEnabledHandler.ts
import { FieldValue } from "firebase-admin/firestore";
import { Request, Response } from "express";
import { initDeps } from "./dependencies.js";
import { getAuth } from "firebase-admin/auth";

export async function biometricsRequiredHandler(req: Request, res: Response) {
    const { firestore } = initDeps();

    try {
        const authHeader = req.headers.authorization;
        if (!authHeader?.startsWith("Bearer ")) {
            return res.status(401).json({ error: "Missing or invalid token" });
        }

        const idToken = authHeader.split("Bearer ")[1];
        const decodedToken = await getAuth().verifyIdToken(idToken);
        const uid = decodedToken.uid;

        const secRef = firestore
            .collection("users")
            .doc(uid)
            .collection("security")
            .doc("settings");

        await secRef.set(
            {
                biometricsRequired: false,
                biometricsEnabledAt: FieldValue.serverTimestamp(),
                updatedAt: FieldValue.serverTimestamp(),
            },
            { merge: true }
        );

        return res.status(200).json({ ok: true });
    } catch (err) {
        console.error("Error in setPassCodeEnabledHandler:", err);
        return res.status(500).json({ error: "Internal server error" });
    }
}

export async function getBiometricsRequiredHandler(req: Request, res: Response) {
  const { firestore } = initDeps();

  try {
    const authHeader = req.headers.authorization;
    if (!authHeader?.startsWith("Bearer ")) {
      return res.status(401).json({ error: "Missing or invalid token" });
    }

    const idToken = authHeader.split("Bearer ")[1];
    const decodedToken = await getAuth().verifyIdToken(idToken);
    const uid = decodedToken.uid;

    const secRef = firestore
      .collection("users")
      .doc(uid)
      .collection("security")
      .doc("settings");

    const snap = await secRef.get();
    const existingData = snap.data();

    if (existingData?.biometricsRequired === false) {
      // Already enabled, no write needed
      return res.status(200).json({ ok: true, message: "Biometrics already enabled" });
    }

    if (!snap.exists) {
      // Default: biometrics not required
      return res.status(200).json({ biometricsRequired: false });
    }

    const data = snap.data() || {};
    return res.status(200).json({
      biometricsRequired: Boolean(data.biometricsRequired),
    });
  } catch (err) {
    console.error("Error in getBiometricsRequiredHandler:", err);
    return res.status(500).json({ error: "Internal server error" });
  }
}