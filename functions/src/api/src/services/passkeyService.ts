import {
  generateAuthenticationOptions,
  generateRegistrationOptions,
  verifyAuthenticationResponse,
  verifyRegistrationResponse,
} from "@simplewebauthn/server";
import { PasskeyCredentialSummary } from "../domain/model/passkey.js";
import { PasskeyRepository } from "../domain/repository/PasskeyRepository.js";
import { normalizePasskeyOrigins } from "../utils/passkeyOrigin.js";

type PasskeyServiceConfig = {
  rpId: string;
  rpName: string;
  expectedOrigins: Set<string>;
  challengeTtlMs: number;
  enabled: boolean;
};

export type PasskeyVerificationResult = {
  verified: boolean;
  credentialId: string;
};

export type PasskeySignInResult = PasskeyVerificationResult & {
  uid: string;
};

export type PasskeyRevokeResult = {
  revoked: boolean;
  credentialId: string;
};

export type PasskeyCredentialsListResult = {
  credentials: PasskeyCredentialSummary[];
};

export class PasskeyService {
  private readonly expectedOrigins: string[];

  constructor(
    private readonly passkeys: PasskeyRepository,
    private readonly config: PasskeyServiceConfig
  ) {
    this.expectedOrigins = normalizePasskeyOrigins(config.expectedOrigins);
  }

  async beginRegistration(
    uid: string,
    input: { userName: string; userDisplayName: string }
  ) {
    this.assertEnabled();
    const credentials = await this.passkeys.listCredentials(uid);

    const options = await generateRegistrationOptions({
      rpID: this.config.rpId,
      rpName: this.config.rpName,
      userID: Buffer.from(uid, "utf8"),
      userName: input.userName,
      userDisplayName: input.userDisplayName,
      timeout: 60_000,
      attestationType: "none",
      authenticatorSelection: {
        residentKey: "required",
        userVerification: "required",
      },
      excludeCredentials: credentials.map((credential) => ({
        id: credential.credentialId,
        transports: credential.transports as any,
      })),
    });

    await this.passkeys.saveChallenge(uid, {
      kind: "registration",
      challenge: options.challenge,
      expiresAtMs: Date.now() + this.config.challengeTtlMs,
    });

    return options;
  }

  async completeRegistration(uid: string, credential: unknown) {
    this.assertEnabled();
    const challenge = await this.consumeChallenge(uid, "registration");
    const credentialJson = parseCredentialPayload(credential);

    const verification = await verifyRegistrationResponse({
      response: credentialJson as any,
      expectedChallenge: challenge,
      expectedOrigin: this.expectedOrigins,
      expectedRPID: this.config.rpId,
      requireUserVerification: true,
    });

    if (!verification.verified || !verification.registrationInfo) {
      throw new Error("PASSKEY_REGISTRATION_VERIFICATION_FAILED");
    }

    const now = Date.now();
    const registrationInfo = verification.registrationInfo;
    const registeredCredential = registrationInfo.credential;
    await this.passkeys.upsertCredential(uid, {
      credentialId: registeredCredential.id,
      publicKeyBase64Url: Buffer.from(registeredCredential.publicKey).toString(
        "base64url"
      ),
      counter: registeredCredential.counter,
      transports: (registeredCredential.transports ?? []) as string[],
      deviceType: registrationInfo.credentialDeviceType,
      backedUp: registrationInfo.credentialBackedUp,
      createdAtMs: now,
      updatedAtMs: now,
    });

    return {
      verified: true,
      credentialId: registeredCredential.id,
    } satisfies PasskeyVerificationResult;
  }

  async beginAuthentication(uid: string) {
    this.assertEnabled();
    const credentials = await this.passkeys.listCredentials(uid);
    if (credentials.length === 0) {
      throw new Error("PASSKEY_CREDENTIALS_NOT_FOUND");
    }

    const options = await generateAuthenticationOptions({
      rpID: this.config.rpId,
      allowCredentials: credentials.map((credential) => ({
        id: credential.credentialId,
        transports: credential.transports as any,
      })),
      userVerification: "required",
      timeout: 60_000,
    });

    await this.passkeys.saveChallenge(uid, {
      kind: "authentication",
      challenge: options.challenge,
      expiresAtMs: Date.now() + this.config.challengeTtlMs,
    });

    return options;
  }

  async beginSignIn() {
    this.assertEnabled();

    const options = await generateAuthenticationOptions({
      rpID: this.config.rpId,
      userVerification: "required",
      timeout: 60_000,
    });

    await this.passkeys.saveSignInChallenge(
      options.challenge,
      Date.now() + this.config.challengeTtlMs
    );

    return options;
  }

  async completeAuthentication(uid: string, credential: unknown) {
    this.assertEnabled();
    const challenge = await this.consumeChallenge(uid, "authentication");
    const credentialJson = parseCredentialPayload(credential);
    const credentialIdCandidates = extractCredentialIdCandidates(credentialJson);
    if (credentialIdCandidates.length === 0) {
      throw new Error("PASSKEY_CREDENTIAL_ID_MISSING");
    }

    const stored = await findUserCredentialByCandidates(
      this.passkeys,
      uid,
      credentialIdCandidates
    );
    if (!stored) {
      throw new Error("PASSKEY_CREDENTIAL_NOT_REGISTERED");
    }

    const verification = await verifyAuthenticationResponse({
      response: credentialJson as any,
      expectedChallenge: challenge,
      expectedOrigin: this.expectedOrigins,
      expectedRPID: this.config.rpId,
      credential: {
        id: stored.credentialId,
        publicKey: Buffer.from(stored.publicKeyBase64Url, "base64url"),
        counter: stored.counter,
        transports: stored.transports as any,
      },
      requireUserVerification: true,
    });

    if (!verification.verified) {
      throw new Error("PASSKEY_AUTHENTICATION_VERIFICATION_FAILED");
    }

    await this.passkeys.updateCounter(
      uid,
      stored.credentialId,
      verification.authenticationInfo.newCounter
    );

    return {
      verified: true,
      credentialId: stored.credentialId,
    } satisfies PasskeyVerificationResult;
  }

  async completeSignIn(credential: unknown): Promise<PasskeySignInResult> {
    this.assertEnabled();
    const credentialJson = parseCredentialPayload(credential);
    const challenge = extractChallengeFromCredential(credentialJson);
    const storedChallenge = await this.passkeys.consumeSignInChallenge(challenge);
    if (!storedChallenge) {
      throw new Error("PASSKEY_CHALLENGE_MISSING");
    }
    if (storedChallenge.expiresAtMs < Date.now()) {
      throw new Error("PASSKEY_CHALLENGE_EXPIRED");
    }

    const credentialIdCandidates = extractCredentialIdCandidates(credentialJson);
    if (credentialIdCandidates.length === 0) {
      throw new Error("PASSKEY_CREDENTIAL_ID_MISSING");
    }

    const owner = await findCredentialOwnerByCandidates(
      this.passkeys,
      credentialIdCandidates
    );
    if (!owner) {
      throw new Error("PASSKEY_CREDENTIAL_NOT_REGISTERED");
    }

    const verification = await verifyAuthenticationResponse({
      response: credentialJson as any,
      expectedChallenge: storedChallenge.challenge,
      expectedOrigin: this.expectedOrigins,
      expectedRPID: this.config.rpId,
      credential: {
        id: owner.credential.credentialId,
        publicKey: Buffer.from(owner.credential.publicKeyBase64Url, "base64url"),
        counter: owner.credential.counter,
        transports: owner.credential.transports as any,
      },
      requireUserVerification: true,
    });

    if (!verification.verified) {
      throw new Error("PASSKEY_AUTHENTICATION_VERIFICATION_FAILED");
    }

    await this.passkeys.updateCounter(
      owner.uid,
      owner.credential.credentialId,
      verification.authenticationInfo.newCounter
    );

    return {
      verified: true,
      credentialId: owner.credential.credentialId,
      uid: owner.uid,
    } satisfies PasskeySignInResult;
  }

  async revokeCredential(uid: string, credentialId: string): Promise<PasskeyRevokeResult> {
    const cleanCredentialId = credentialId.trim();
    if (!cleanCredentialId) {
      throw new Error("PASSKEY_CREDENTIAL_ID_MISSING");
    }

    const existing = await this.passkeys.getCredential(uid, cleanCredentialId);
    if (!existing) {
      throw new Error("PASSKEY_CREDENTIAL_NOT_REGISTERED");
    }

    await this.passkeys.deleteCredential(uid, cleanCredentialId);
    return {
      revoked: true,
      credentialId: cleanCredentialId,
    };
  }

  async listCredentials(uid: string): Promise<PasskeyCredentialsListResult> {
    const credentials = await this.passkeys.listCredentials(uid);
    const sanitized: PasskeyCredentialSummary[] = credentials
      .map((credential) => ({
        credentialId: credential.credentialId,
        deviceType: credential.deviceType,
        backedUp: credential.backedUp,
        transports: credential.transports,
        createdAtMs: credential.createdAtMs,
        updatedAtMs: credential.updatedAtMs,
      }))
      .sort((left, right) => right.updatedAtMs - left.updatedAtMs);

    return { credentials: sanitized };
  }

  private async consumeChallenge(
    uid: string,
    kind: "registration" | "authentication"
  ): Promise<string> {
    const stored = await this.passkeys.consumeChallenge(uid, kind);
    if (!stored) {
      throw new Error("PASSKEY_CHALLENGE_MISSING");
    }
    if (stored.expiresAtMs < Date.now()) {
      throw new Error("PASSKEY_CHALLENGE_EXPIRED");
    }
    return stored.challenge;
  }

  private assertEnabled() {
    if (!this.config.enabled || this.expectedOrigins.length === 0) {
      throw new Error("PASSKEY_NOT_CONFIGURED");
    }
  }
}

function parseCredentialPayload(input: unknown): Record<string, unknown> {
  if (typeof input === "string") {
    const trimmed = input.trim();
    if (!trimmed) throw new Error("Missing credential");
    try {
      return JSON.parse(trimmed) as Record<string, unknown>;
    } catch {
      throw new Error("Invalid credential JSON");
    }
  }
  if (input && typeof input === "object") {
    return input as Record<string, unknown>;
  }
  throw new Error("Missing credential");
}

async function findUserCredentialByCandidates(
  passkeys: PasskeyRepository,
  uid: string,
  candidates: string[]
) {
  for (const candidate of candidates) {
    const stored = await passkeys.getCredential(uid, candidate);
    if (stored) {
      return stored;
    }
  }
  return null;
}

async function findCredentialOwnerByCandidates(
  passkeys: PasskeyRepository,
  candidates: string[]
) {
  for (const candidate of candidates) {
    const owner = await passkeys.getCredentialOwner(candidate);
    if (owner) {
      return owner;
    }
  }
  return null;
}

function extractCredentialIdCandidates(
  credentialJson: Record<string, unknown>
): string[] {
  const raw = [
    (credentialJson.id ?? "").toString().trim(),
    (credentialJson.rawId ?? "").toString().trim(),
  ].filter((value) => value.length > 0);

  const unique = new Set<string>();
  for (const value of raw) {
    for (const normalized of normalizeCredentialIdVariants(value)) {
      unique.add(normalized);
    }
  }
  return Array.from(unique);
}

function normalizeCredentialIdVariants(value: string): string[] {
  const trimmed = value.trim();
  if (!trimmed) return [];

  const variants = new Set<string>();
  variants.add(trimmed);

  // Attempt canonical Base64URL without padding from both URL-safe and standard encodings.
  if (/^[A-Za-z0-9+/_=-]+$/.test(trimmed)) {
    try {
      variants.add(Buffer.from(trimmed, "base64url").toString("base64url"));
    } catch {
      // Ignore decode issues and keep original form.
    }
    try {
      variants.add(Buffer.from(trimmed, "base64").toString("base64url"));
    } catch {
      // Ignore decode issues and keep original form.
    }
  }

  return Array.from(variants).filter((entry) => entry.length > 0);
}

function extractChallengeFromCredential(
  credentialJson: Record<string, unknown>
): string {
  const response = credentialJson.response;
  if (!response || typeof response !== "object") {
    throw new Error("PASSKEY_CHALLENGE_MISSING");
  }

  const clientDataJson = (response as Record<string, unknown>).clientDataJSON;
  const encoded = (clientDataJson ?? "").toString().trim();
  if (!encoded) {
    throw new Error("PASSKEY_CHALLENGE_MISSING");
  }

  try {
    const decodedClientData = Buffer.from(encoded, "base64url").toString("utf8");
    const parsed = JSON.parse(decodedClientData) as Record<string, unknown>;
    const challenge = (parsed.challenge ?? "").toString().trim();
    if (!challenge) {
      throw new Error("PASSKEY_CHALLENGE_MISSING");
    }
    return challenge;
  } catch {
    throw new Error("PASSKEY_CHALLENGE_MISSING");
  }
}
