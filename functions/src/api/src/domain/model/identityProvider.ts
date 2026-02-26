export type IdentityProviderStatus =
  | "session_created"
  | "in_progress"
  | "pending_review"
  | "verified"
  | "rejected"
  | "cancelled";

export interface StartIdentityProviderSessionInput {
  countryIso2?: string;
  documentType?: string;
}

export interface IdentityProviderSession {
  sessionId: string;
  provider: string;
  status: IdentityProviderStatus;
  launchUrl?: string;
  expiresAtMs?: number;
}

export interface ResumeIdentityProviderSessionInput {
  sessionId: string;
}

export interface IdentityProviderSessionResume {
  sessionId: string;
  provider: string;
  status: IdentityProviderStatus;
  launchUrl?: string;
  reason?: string;
  updatedAtMs?: number;
}

export interface IdentityProviderCallbackInput {
  event: string;
  sessionId?: string;
  providerRef?: string;
  rawDeepLink?: string;
}
