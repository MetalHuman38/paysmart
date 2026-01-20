// src/handlers/setPasswordEnabledHandler.ts
import { FieldValue } from "firebase-admin/firestore";
import { Request, Response } from "express";
import { initDeps } from "./dependencies.js";
import { getAuth } from "firebase-admin/auth";

export async function setPassCodeEnabledHandler(req: Request, res: Response) {
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
                passcodeEnabled: true,
                localPassCodeSetAt: FieldValue.serverTimestamp(),
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

export async function getPassCodeEnabledHandler(req: Request, res: Response) {
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

    if (existingData?.passcodeEnabled === true) {
      // Already enabled, no write needed
      return res.status(200).json({ ok: true, message: "Passcode already enabled" });
    }

    if (!snap.exists) {
      // Default: passcode not enabled
      return res.status(200).json({ passcodeEnabled: false });
    }

    const data = snap.data() || {};
    return res.status(200).json({
      passcodeEnabled: Boolean(data.passcodeEnabled),
    });
  } catch (err) {
    console.error("Error in getPassCodeEnabledHandler:", err);
    return res.status(500).json({ error: "Internal server error" });
  }
}