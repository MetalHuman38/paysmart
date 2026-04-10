import { FieldValue } from "firebase-admin/firestore";
// Canonical recovery-first defaults — ownership lives in the auth codebase.
// This fallback is a safety net for the rare case the auth beforeCreate hook
// has not yet run when UpsertVerifiedPhoneUser executes.
const CANONICAL_SECURITY_DEFAULTS = {
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
export class FirestoreSecuritySettingsRepository {
    firestore;
    constructor(firestore) {
        this.firestore = firestore;
    }
    ref(uid) {
        return this.firestore
            .collection("users")
            .doc(uid)
            .collection("security")
            .doc("settings");
    }
    async get(uid) {
        const snap = await this.ref(uid).get();
        return snap.exists ? snap.data() : null;
    }
    async createIfMissing(uid) {
        const ref = this.ref(uid);
        await this.firestore.runTransaction(async (tx) => {
            const snap = await tx.get(ref);
            if (!snap.exists) {
                tx.set(ref, CANONICAL_SECURITY_DEFAULTS);
            }
        });
    }
    async update(uid, data) {
        await this.ref(uid).set(data, { merge: true });
    }
}
//# sourceMappingURL=FirestoreSecuritySettingsRepository.js.map