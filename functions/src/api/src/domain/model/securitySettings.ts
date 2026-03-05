export interface SecuritySettingsModel {
  allowFederatedLinking: boolean;
  passcodeEnabled: boolean;
  passwordEnabled: boolean;
  passkeyEnabled: boolean;
  biometricsRequired: boolean;
  biometricsEnabled: boolean;
  biometricsEnabledAt: FirebaseFirestore.Timestamp | FirebaseFirestore.FieldValue | null;
  lockAfterMinutes: number;
  onboardingRequired: Record<string, boolean>;
  onboardingCompleted: Record<string, boolean>;
  emailToVerify: string | null;
  emailVerificationSentAt: FirebaseFirestore.Timestamp | null;
  emailVerificationAttemptsToday: number;
  hasVerifiedEmail: boolean;
  hasAddedHomeAddress: boolean;
  hasVerifiedIdentity: boolean;
  hasSkippedMfaEnrollmentPrompt: boolean;
  hasSkippedPasskeyEnrollmentPrompt: boolean;
  hasEnrolledMfaFactor: boolean;
  mfaEnrolledAt: FirebaseFirestore.Timestamp | FirebaseFirestore.FieldValue | null;
  kycStatus?: string | null;
  localPassCodeSetAt: FirebaseFirestore.Timestamp | null;
  localPasswordSetAt: FirebaseFirestore.Timestamp | null;
  updatedAt: FirebaseFirestore.FieldValue;
}
