// src/handlers/setPasswordEnabledHandler.ts
import { FieldValue } from "firebase-admin/firestore";
import { Request, Response } from "express";
import { initDeps } from "./dependencies.js";
import { getAuth } from "firebase-admin/auth";

export async function setPasswordEnabledHandler(req: Request, res: Response) {
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
                passwordEnabled: true,
                localPasswordSetAt: FieldValue.serverTimestamp(),
                updatedAt: FieldValue.serverTimestamp(),
            },
            { merge: true }
        );

        return res.status(200).json({ ok: true });
    } catch (err) {
        console.error("Error in setPasswordEnabledHandler:", err);
        return res.status(500).json({ error: "Internal server error" });
    }
}

export async function getPasswordEnabledHandler(req: Request, res: Response) {
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

    if (!snap.exists) {
      // Default: password not enabled
      return res.status(200).json({ passwordEnabled: false });
    }

    const data = snap.data() || {};
    return res.status(200).json({
      passwordEnabled: Boolean(data.passwordEnabled),
    });
  } catch (err) {
    console.error("Error in getPasswordEnabledHandler:", err);
    return res.status(500).json({ error: "Internal server error" });
  }
}