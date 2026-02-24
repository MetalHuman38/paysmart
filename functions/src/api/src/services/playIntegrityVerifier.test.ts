import { describe, expect, it, vi } from "vitest";
import { PlayIntegrityVerifier } from "./playIntegrityVerifier.js";

describe("PlayIntegrityVerifier", () => {
  it("accepts a valid Play Integrity payload", async () => {
    const tokenProvider = {
      getAccessToken: vi.fn().mockResolvedValue("access-token"),
    };
    const fetchLike = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ({
        tokenPayloadExternal: {
          requestDetails: {
            requestPackageName: "net.metalbrain.paysmart",
            nonce: "nonce-1",
            timestampMillis: String(Date.now()),
          },
          appIntegrity: {
            appRecognitionVerdict: "PLAY_RECOGNIZED",
          },
          deviceIntegrity: {
            deviceRecognitionVerdict: ["MEETS_DEVICE_INTEGRITY"],
          },
          accountDetails: {
            appLicensingVerdict: "LICENSED",
          },
        },
      }),
      text: async () => "",
    });

    const verifier = new PlayIntegrityVerifier(
      tokenProvider,
      {
        packageName: "net.metalbrain.paysmart",
        maxAgeMs: 60_000,
        allowFallback: false,
        allowedAppVerdicts: new Set(["play_recognized"]),
        allowedDeviceVerdicts: new Set(["meets_device_integrity"]),
        requireLicensed: true,
      },
      fetchLike
    );

    const result = await verifier.verify("attestation.jwt.token", "nonce-1");

    expect(tokenProvider.getAccessToken).toHaveBeenCalled();
    expect(fetchLike).toHaveBeenCalled();
    expect(result.provider).toBe("play_integrity");
    expect(result.packageName).toBe("net.metalbrain.paysmart");
    expect(result.deviceRecognitionVerdicts).toEqual([
      "MEETS_DEVICE_INTEGRITY",
    ]);
  });

  it("rejects payload when nonce mismatches", async () => {
    const tokenProvider = {
      getAccessToken: vi.fn().mockResolvedValue("access-token"),
    };
    const fetchLike = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ({
        tokenPayloadExternal: {
          requestDetails: {
            requestPackageName: "net.metalbrain.paysmart",
            nonce: "wrong-nonce",
            timestampMillis: String(Date.now()),
          },
          appIntegrity: {
            appRecognitionVerdict: "PLAY_RECOGNIZED",
          },
          deviceIntegrity: {
            deviceRecognitionVerdict: ["MEETS_DEVICE_INTEGRITY"],
          },
        },
      }),
      text: async () => "",
    });

    const verifier = new PlayIntegrityVerifier(
      tokenProvider,
      {
        packageName: "net.metalbrain.paysmart",
        maxAgeMs: 60_000,
        allowFallback: false,
        allowedAppVerdicts: new Set(["play_recognized"]),
        allowedDeviceVerdicts: new Set(["meets_device_integrity"]),
        requireLicensed: false,
      },
      fetchLike
    );

    await expect(
      verifier.verify("attestation.jwt.token", "nonce-1")
    ).rejects.toThrow("nonce mismatch");
  });

  it("accepts fallback token when enabled", async () => {
    const tokenProvider = {
      getAccessToken: vi.fn(),
    };
    const verifier = new PlayIntegrityVerifier(
      tokenProvider,
      {
        packageName: "net.metalbrain.paysmart",
        maxAgeMs: 60_000,
        allowFallback: true,
        allowedAppVerdicts: new Set(["play_recognized"]),
        allowedDeviceVerdicts: new Set(["meets_device_integrity"]),
        requireLicensed: false,
      }
    );

    const fallbackPayload = Buffer.from(
      JSON.stringify({
        nonce: "nonce-1",
        issuedAt: Date.now(),
      })
    ).toString("base64url");
    const token = `fallback.${fallbackPayload}.signature`;

    const result = await verifier.verify(token, "nonce-1");

    expect(result.provider).toBe("fallback");
    expect(tokenProvider.getAccessToken).not.toHaveBeenCalled();
  });
});
