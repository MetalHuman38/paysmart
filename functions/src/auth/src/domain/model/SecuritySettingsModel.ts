export interface SecuritySettingsModel {
  allowFederatedLinking: boolean;
  providerIds?: string[];
  passcodeEnabled: boolean;
  passwordEnabled: boolean;
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
  localPassCodeSetAt: FirebaseFirestore.Timestamp | null;
  localPasswordSetAt: FirebaseFirestore.Timestamp | null;
  updatedAt: FirebaseFirestore.FieldValue;
}
