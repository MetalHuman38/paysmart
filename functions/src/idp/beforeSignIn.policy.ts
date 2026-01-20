import { HttpsError } from "firebase-functions/v2/https";
import { AuthBlockingEvent } from "firebase-functions/v2/identity";
import { FieldValue } from "firebase-admin/firestore";
import { initDeps } from "../dependencies.js";
import { logEvent } from "../utils.js";
import { DEFAULT_SECURITY } from "./constant.js";

const deps = initDeps();
export async function beforeSignInPolicy(event: AuthBlockingEvent) {
  const user = event.data;
  const uid = user?.uid;
  const ts = Math.floor(Date.now() / 1000);

  if (!uid) {
    throw new HttpsError("invalid-argument", "Missing UID");
  }

  const firestore = deps.firestore;
  const userRef = firestore.collection("users").doc(uid);
  const secRef = userRef.collection("security").doc("settings");

  // 🔑 1. Ensure security exists (race-safe)
  let security: any;

  await firestore.runTransaction(async (tx) => {
    const secSnap = await tx.get(secRef);

    if (!secSnap.exists) {
      tx.set(secRef, {
        ...DEFAULT_SECURITY,
        updatedAt: FieldValue.serverTimestamp(),
      });

      security = { ...DEFAULT_SECURITY };
    } else {
      security = secSnap.data();
    }
  });

  const providerIds = user.providerData.map((p) => p.providerId);

  const hasPhone = providerIds.includes("phone");
  const hasFederated = providerIds.some((id) =>
    ["google.com", "facebook.com", "password"].includes(id)
  );

  /* -------------------------------------------------
   * 2️⃣ BLOCK: Federated-only login
   * ------------------------------------------------- */
  if (hasFederated && !hasPhone) {
    throw new HttpsError(
      "permission-denied",
      "Federated login requires verified phone number"
    );
  }

  /* -------------------------------------------------
   * 3️⃣ PASSWORD RULES
   * ------------------------------------------------- */
  const usesPassword = providerIds.includes("password");

  if (usesPassword) {
    if (!security.passwordEnabled) {
      throw new HttpsError("permission-denied", "Password login disabled");
    }
    if (!security.hasVerifiedEmail) {
      throw new HttpsError("permission-denied", "Email not verified");
    }
  }

  /* -------------------------------------------------
   * 4️⃣ FEDERATED EMAIL SYNC
   * ------------------------------------------------- */
  if (user.email && !security.hasVerifiedEmail) {
    const primaryProvider =
      providerIds.find((p) =>
        ["google.com", "facebook.com", "password"].includes(p)
      ) ?? "phone";

    await Promise.all([
      userRef.set(
        {
          email: user.email,
          authProvider: primaryProvider.replace(".com", ""),
          providerIds,
          lastSignedIn: FieldValue.serverTimestamp(),
        },
        { merge: true }
      ),

      secRef.update({
        hasVerifiedEmail: true,
        emailToVerify: FieldValue.delete(),
        updatedAt: FieldValue.serverTimestamp(),
      }),

      firestore.collection("audit_logs").add({
        type: "email_verified",
        uid,
        email: user.email,
        provider: providerIds,
        createdAt: FieldValue.serverTimestamp(),
      }),
    ]);

    logEvent("email-verification:confirmed", {
      uid,
      email: user.email,
      providerIds,
    });
  }

  /* -------------------------------------------------
   * 5️⃣ SESSION CLAIMS
   * ------------------------------------------------- */
  return {
    sessionClaims: {
      ts,
      emailVerified: !!security.hasVerifiedEmail,
    },
  };
}
