import { Firestore, FieldValue } from "firebase-admin/firestore";
import { SecuritySettingsModel } from "../../domain/model/securitySettings.js";
import { SecuritySettingsRepository } from "../../domain/repository/SecuritySettingsRepository.js";

// Canonical recovery-first defaults — ownership lives in the auth codebase.
// This fallback is a safety net for the rare case the auth beforeCreate hook
// has not yet run when UpsertVerifiedPhoneUser executes.
const CANONICAL_SECURITY_DEFAULTS: SecuritySettingsModel = {
  allowFederatedLinking: false,
  passcodeEnabled: false,
  passwordEnabled: false,
  passkeyEnabled: false,
  biometricsRequired: false,
  biometricsEnabled: false,
  biometricsEnabledAt: null,
  lockAfterMinutes: 5,
  onboardingRequired: {},
  onboardingCompleted: {},
  emailToVerify: null,
  emailVerificationSentAt: null,
  emailVerificationAttemptsToday: 0,
  hasVerifiedEmail: false,
  hasAddedHomeAddress: false,
  hasVerifiedIdentity: false,
  hasSkippedMfaEnrollmentPrompt: false,
  hasSkippedPasskeyEnrollmentPrompt: true,
  hasEnrolledMfaFactor: false,
  mfaEnrolledAt: null,
  kycStatus: null,
  localPassCodeSetAt: null,
  localPasswordSetAt: null,
  updatedAt: FieldValue.serverTimestamp(),
};

export class FirestoreSecuritySettingsRepository implements SecuritySettingsRepository {
  constructor(private readonly firestore: Firestore) {}

  private ref(uid: string) {
    return this.firestore
      .collection("users")
      .doc(uid)
      .collection("security")
      .doc("settings");
  }

  async get(uid: string): Promise<SecuritySettingsModel | null> {
    const snap = await this.ref(uid).get();
    return snap.exists ? (snap.data() as SecuritySettingsModel) : null;
  }

  async createIfMissing(uid: string): Promise<void> {
    const ref = this.ref(uid);

    await this.firestore.runTransaction(async (tx) => {
      const snap = await tx.get(ref);
      if (!snap.exists) {
        tx.set(ref, CANONICAL_SECURITY_DEFAULTS);
      }
    });
  }

  async update(uid: string, data: Partial<SecuritySettingsModel>): Promise<void> {
    await this.ref(uid).set(data, { merge: true });
  }
}
