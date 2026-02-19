import { Auth } from "firebase-admin/auth";
import { FirebaseAuthServiceInterface } from "./FirebaseAuthServiceInterface.js";

export class FirebaseAuthService implements FirebaseAuthServiceInterface {
  constructor(private readonly auth: Auth) {}

  async getUserByPhone(phoneNumber: string): Promise<{ uid: string } | null> {
    try {
      const userRecord = await this.auth.getUserByPhoneNumber(phoneNumber);
      return { uid: userRecord.uid };
    } catch (error) {
      const code = (error as any).code;
      if (code === "auth/user-not-found") {
        return null;
      }
      throw error;
    }
  }

  async getUserByEmail(
    email: string
  ): Promise<{ uid: string; phoneNumber?: string | null } | null> {
    try {
      const userRecord = await this.auth.getUserByEmail(email);
      return { uid: userRecord.uid, phoneNumber: userRecord.phoneNumber ?? null };
    } catch (error) {
      const code = (error as any).code;
      if (code === "auth/user-not-found") {
        return null;
      }
      throw error;
    }
  }

  async getUserByUid(
    uid: string
  ): Promise<{ uid: string; phoneNumber?: string | null; providerIds?: string[] } | null> {
    try {
      const userRecord = await this.auth.getUser(uid);
      return {
        uid: userRecord.uid,
        phoneNumber: userRecord.phoneNumber ?? null,
        providerIds: (userRecord.providerData ?? [])
          .map((provider) => provider.providerId)
          .filter((providerId): providerId is string => Boolean(providerId)),
      };
    } catch (error) {
      const code = (error as any).code;
      if (code === "auth/user-not-found") {
        return null;
      }
      throw error;
    }
  }
}
