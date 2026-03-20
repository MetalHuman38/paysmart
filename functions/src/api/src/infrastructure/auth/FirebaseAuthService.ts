import { Auth } from "firebase-admin/auth";

export interface AuthService {
  verifyIdToken(idToken: string): Promise<{ uid: string; email?: string }>;
  getUser(
    uid: string
  ): Promise<{
    uid: string;
    email?: string;
    phoneNumber?: string;
    emailVerified?: boolean;
    isAnonymous?: boolean;
    providerIds?: string[];
    tenantId?: string | null;
    photoURL?: string;
    displayName?: string;
  }>;
  getUserByPhone(phone: string): Promise<{ uid: string } | null>;
  updateUserEmail(uid: string, email: string): Promise<void>;
  generateEmailVerificationLink(
    email: string,
    continueUrl: string
  ): Promise<string>;
}

export class FirebaseAuthService implements AuthService {
  constructor(private readonly auth: Auth) {}

  async verifyIdToken(idToken: string) {
    const decoded = await this.auth.verifyIdToken(idToken);
    return {
      uid: decoded.uid,
      email: decoded.email,
    };
  }

  async getUser(uid: string) {
    const user = await this.auth.getUser(uid);
    return {
      uid: user.uid,
      email: user.email ?? undefined,
      phoneNumber: user.phoneNumber ?? undefined,
      emailVerified: user.emailVerified,
      isAnonymous: user.providerData.length === 0,
      providerIds: user.providerData.map((provider) => provider.providerId),
      tenantId: user.tenantId ?? null,
      photoURL: user.photoURL ?? undefined,
      displayName: user.displayName ?? undefined,
    };
  }

  async updateUserEmail(uid: string, email: string) {
    await this.auth.updateUser(uid, {
      email,
      emailVerified: false,
    });
  }

  async getUserByPhone(phone: string): Promise<{ uid: string } | null> {
    try {
      const user = await this.auth.getUserByPhoneNumber(phone);
      return { uid: user.uid };
    } catch (err: any) {
      if (err?.code === "auth/user-not-found") {
        return null;
      }
      throw err;
    }
  }

  async generateEmailVerificationLink(
    email: string,
    continueUrl: string
  ): Promise<string> {
    return this.auth.generateEmailVerificationLink(email, {
      url: continueUrl,
      handleCodeInApp: false,
    });
  }
}
