import { createHash } from "crypto";
import { CloudAccessTokenProvider } from "./googleCloudAccessTokenProvider.js";

const PLAY_INTEGRITY_SCOPE = "https://www.googleapis.com/auth/playintegrity";
const PLAY_INTEGRITY_ENDPOINT = "https://playintegrity.googleapis.com/v1";

type FetchResponseLike = {
  ok: boolean;
  status: number;
  json(): Promise<unknown>;
  text(): Promise<string>;
};

type FetchLike = (
  url: string,
  init: {
    method: "POST";
    headers: Record<string, string>;
    body: string;
  }
) => Promise<FetchResponseLike>;

type PlayIntegrityPayload = {
  requestDetails?: {
    requestPackageName?: string;
    nonce?: string;
    timestampMillis?: string;
  };
  appIntegrity?: {
    appRecognitionVerdict?: string;
  };
  deviceIntegrity?: {
    deviceRecognitionVerdict?: string[];
  };
  accountDetails?: {
    appLicensingVerdict?: string;
  };
};

export type AttestationVerificationResult = {
  provider: "play_integrity" | "fallback";
  packageName: string;
  nonce: string;
  timestampMillis: number;
  appRecognitionVerdict: string | null;
  deviceRecognitionVerdicts: string[];
  appLicensingVerdict: string | null;
  attestationDigest: string;
};

export type PlayIntegrityVerifierOptions = {
  packageName: string;
  maxAgeMs: number;
  allowFallback: boolean;
  allowedAppVerdicts: Set<string>;
  allowedDeviceVerdicts: Set<string>;
  requireLicensed: boolean;
};

export class PlayIntegrityVerifier {
  constructor(
    private readonly tokenProvider: CloudAccessTokenProvider,
    private readonly options: PlayIntegrityVerifierOptions,
    private readonly fetchLike: FetchLike = fetch as unknown as FetchLike
  ) {}

  async verify(
    attestationToken: string,
    expectedNonce: string
  ): Promise<AttestationVerificationResult> {
    const token = attestationToken.trim();
    if (!token) {
      throw new Error("Missing attestationJwt");
    }

    if (token.startsWith("fallback.")) {
      return this.verifyFallbackToken(token, expectedNonce);
    }

    if (!this.options.packageName) {
      throw new Error("PLAY_INTEGRITY_PACKAGE_NAME is not configured");
    }

    const accessToken = await this.tokenProvider.getAccessToken([
      PLAY_INTEGRITY_SCOPE,
    ]);
    const response = await this.fetchLike(
      `${PLAY_INTEGRITY_ENDPOINT}/${this.options.packageName}:decodeIntegrityToken`,
      {
        method: "POST",
        headers: {
          Authorization: `Bearer ${accessToken}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          integrity_token: token,
        }),
      }
    );

    if (!response.ok) {
      const errorBody = await response.text();
      throw new Error(
        `Play Integrity decode failed (${response.status}): ${errorBody}`
      );
    }

    const payload = (await response.json()) as {
      tokenPayloadExternal?: PlayIntegrityPayload;
    };
    const tokenPayload = payload.tokenPayloadExternal;
    if (!tokenPayload) {
      throw new Error("Play Integrity payload is missing tokenPayloadExternal");
    }

    return this.validatePayload(tokenPayload, expectedNonce, token);
  }

  private validatePayload(
    payload: PlayIntegrityPayload,
    expectedNonce: string,
    rawToken: string
  ): AttestationVerificationResult {
    const nonce = payload.requestDetails?.nonce ?? "";
    if (!nonce || nonce !== expectedNonce) {
      throw new Error("Play Integrity nonce mismatch");
    }

    const packageName = payload.requestDetails?.requestPackageName ?? "";
    if (!packageName || packageName !== this.options.packageName) {
      throw new Error("Play Integrity package mismatch");
    }

    const timestampMillis = Number(payload.requestDetails?.timestampMillis ?? 0);
    if (!Number.isFinite(timestampMillis) || timestampMillis <= 0) {
      throw new Error("Play Integrity timestamp is invalid");
    }

    const ageMs = Math.abs(Date.now() - timestampMillis);
    if (ageMs > this.options.maxAgeMs) {
      throw new Error("Play Integrity token is expired");
    }

    const appRecognitionVerdictRaw =
      payload.appIntegrity?.appRecognitionVerdict ?? "";
    const appRecognitionVerdict = appRecognitionVerdictRaw
      ? appRecognitionVerdictRaw.toLowerCase()
      : null;

    if (
      !appRecognitionVerdict ||
      !this.options.allowedAppVerdicts.has(appRecognitionVerdict)
    ) {
      throw new Error("Play Integrity app verdict is not allowed");
    }

    const deviceRecognitionVerdicts = (
      payload.deviceIntegrity?.deviceRecognitionVerdict ?? []
    )
      .map((value) => value.toLowerCase())
      .filter(Boolean);
    const hasAllowedDeviceVerdict = deviceRecognitionVerdicts.some((value) =>
      this.options.allowedDeviceVerdicts.has(value)
    );
    if (!hasAllowedDeviceVerdict) {
      throw new Error("Play Integrity device verdict is not allowed");
    }

    const appLicensingVerdictRaw =
      payload.accountDetails?.appLicensingVerdict ?? "";
    const appLicensingVerdict = appLicensingVerdictRaw
      ? appLicensingVerdictRaw.toLowerCase()
      : null;
    if (this.options.requireLicensed && appLicensingVerdict !== "licensed") {
      throw new Error("Play Integrity app licensing verdict is not licensed");
    }

    return {
      provider: "play_integrity",
      packageName,
      nonce,
      timestampMillis,
      appRecognitionVerdict: appRecognitionVerdictRaw || null,
      deviceRecognitionVerdicts:
        payload.deviceIntegrity?.deviceRecognitionVerdict ?? [],
      appLicensingVerdict: appLicensingVerdictRaw || null,
      attestationDigest: createHash("sha256").update(rawToken).digest("hex"),
    };
  }

  private verifyFallbackToken(
    token: string,
    expectedNonce: string
  ): AttestationVerificationResult {
    if (!this.options.allowFallback) {
      throw new Error("Fallback attestation token is disabled");
    }

    const segments = token.split(".");
    if (segments.length < 2) {
      throw new Error("Fallback attestation token format is invalid");
    }

    let parsed: { nonce?: string; issuedAt?: number; packageName?: string };
    try {
      const json = Buffer.from(segments[1], "base64url").toString("utf8");
      parsed = JSON.parse(json) as {
        nonce?: string;
        issuedAt?: number;
        packageName?: string;
      };
    } catch {
      throw new Error("Fallback attestation token is malformed");
    }

    if (!parsed.nonce || parsed.nonce !== expectedNonce) {
      throw new Error("Fallback attestation nonce mismatch");
    }

    return {
      provider: "fallback",
      packageName: parsed.packageName || this.options.packageName || "fallback",
      nonce: parsed.nonce,
      timestampMillis: Number(parsed.issuedAt ?? Date.now()),
      appRecognitionVerdict: "FALLBACK",
      deviceRecognitionVerdicts: ["FALLBACK"],
      appLicensingVerdict: "FALLBACK",
      attestationDigest: createHash("sha256").update(token).digest("hex"),
    };
  }
}
