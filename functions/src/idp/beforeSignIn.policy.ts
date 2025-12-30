import { HttpsError } from "firebase-functions/v2/https";
import { AuthBlockingEvent } from "firebase-functions/v2/identity";
import { initDeps } from "../dependencies.js";

const deps = initDeps();

export async function beforeSignInPolicy(event: AuthBlockingEvent) {
  const user = event.data;
  const uid = user?.uid;
  const ts = Math.floor(Date.now() / 1000);

  if (!uid) {
    throw new HttpsError("invalid-argument", "Missing UID");
  }

  let data: Record<string, any> = {};
  try {
    const security = await deps.firestore
      .collection("users")
      .doc(uid)
      .collection("security")
      .doc("settings")
      .get();
    data = security.exists ? security.data() || {} : {};
  } catch (err: any) {
    throw new HttpsError("internal", "Unable to load security settings");
  }

  const usesPassword = Array.isArray(user.providerData) &&
    user.providerData.some((p) => p.providerId === "password");

  if (usesPassword) {
    if (!data.passwordEnabled) {
      throw new HttpsError("permission-denied", "Password login disabled");
    }

    if (!data.hasVerifiedEmail) {
      throw new HttpsError("permission-denied", "Email not verified");
    }

    return {
      sessionClaims: {
        ts,
        emailVerified: !!data.hasVerifiedEmail,
      },
    };
  }

  return { sessionClaims: { ts } };
}
