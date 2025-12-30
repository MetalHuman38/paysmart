import { beforeUserSignedIn } from "firebase-functions/v2/identity";
import { FieldValue } from "firebase-admin/firestore";
import { initDeps } from "./dependencies.js";
import { logEvent } from "./utils.js";

const REGION = "europe-west2" as const;

/**
 * Sync Firebase Auth email verification -> Firestore security state.
 * Auth has no update trigger; use identity blocking before sign-in instead.
 */
export const syncEmailVerification = beforeUserSignedIn(
  { region: REGION },
  async (event) => {
    const user = event.data;

    // Only act on transition: unverified -> verified
    if (!user?.email || !user.emailVerified) return;

    const { firestore } = initDeps();
    const uid = user.uid;

    const secRef = firestore
      .collection("users")
      .doc(uid)
      .collection("security")
      .doc("settings");

    const snap = await secRef.get();
    if (!snap.exists) return;

    const sec = snap.data()!;
    if (sec.hasVerifiedEmail === true) return;

    await secRef.update({
      hasVerifiedEmail: true,
      emailToVerify: FieldValue.delete(),
      updatedAt: FieldValue.serverTimestamp(),
    });

    await firestore.collection("audit_logs").add({
      type: "email_verified",
      uid,
      email: user.email,
      provider: user.providerData.map((p) => p.providerId),
      createdAt: FieldValue.serverTimestamp(),
    });

    logEvent("email-verification:confirmed", {
      uid,
      email: user.email,
    });
  }
);
