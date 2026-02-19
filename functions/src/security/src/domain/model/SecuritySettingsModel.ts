export interface SecuritySettingsModel {
  allowFederatedLinking: boolean;
  passcodeEnabled: boolean;
  passwordEnabled: boolean;
  biometricsRequired: boolean;
  biometricsEnabled: boolean;
  lockAfterMinutes: number;
  onboardingRequired: Record<string, boolean>;
  onboardingCompleted: Record<string, boolean>;
  hasVerifiedEmail: boolean;
  hasAddedHomeAddress: boolean;
  hasVerifiedIdentity: boolean;
  localPassCodeSetAt: FirebaseFirestore.Timestamp | null;
  localPasswordSetAt: FirebaseFirestore.Timestamp | null;
  updatedAt: FirebaseFirestore.FieldValue;
}
