export interface SecuritySettingsModel {
  allowFederatedLinking: boolean;
  passcodeEnabled: boolean;
  passwordEnabled: boolean;
  passkeyEnabled: boolean;
  biometricsRequired: boolean;
  biometricsEnabled: boolean;
  lockAfterMinutes: number;
  onboardingRequired: Record<string, boolean>;
  onboardingCompleted: Record<string, boolean>;
  hasVerifiedEmail: boolean;
  hasAddedHomeAddress: boolean;
  hasVerifiedIdentity: boolean;
  hasSkippedMfaEnrollmentPrompt: boolean;
  hasSkippedPasskeyEnrollmentPrompt: boolean;
  hasEnrolledMfaFactor: boolean;
  mfaEnrolledAt: FirebaseFirestore.Timestamp | null;
  localPassCodeSetAt: FirebaseFirestore.Timestamp | null;
  localPasswordSetAt: FirebaseFirestore.Timestamp | null;
  updatedAt: FirebaseFirestore.FieldValue;
}
