import { HttpsError } from "firebase-functions/v2/https";
import { AuthBlockingEvent } from "firebase-functions/v2/identity";
import * as admin from "firebase-admin";

export async function beforeCreatePolicy(event: AuthBlockingEvent) {
  const user = event.data;

  if (!user?.email && !user?.phoneNumber) {
    throw new HttpsError("invalid-argument", "Email or phone required");
  }

  // Phone number already in use
  if (user?.phoneNumber) {
    try {
      await admin.auth().getUserByPhoneNumber(user.phoneNumber);
      throw new HttpsError("already-exists", "Phone number already registered");
    } catch (err: any) {
      if (err.code !== "auth/user-not-found") {
        throw new HttpsError("internal", "Phone lookup failed");
      }
    }
  }

  return {
    customClaims: { role: "user" },
    displayName: user.displayName || undefined,
  };
}
