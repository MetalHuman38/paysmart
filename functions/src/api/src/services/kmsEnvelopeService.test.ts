import { describe, expect, it, vi } from "vitest";
import { KmsEnvelopeService } from "./kmsEnvelopeService.js";

describe("KmsEnvelopeService", () => {
  it("fails fast for createEnvelopeKey when kms key is not configured", async () => {
    const tokenProvider = {
      getAccessToken: vi.fn(),
    };
    const fetchLike = vi.fn();

    const service = new KmsEnvelopeService(
      tokenProvider as any,
      "",
      fetchLike as any
    );

    await expect(service.createEnvelopeKey("uid:u1|session:s1|doc:passport")).rejects
      .toThrow("IDENTITY_UPLOAD_KMS_KEY_NAME is not configured");
    expect(tokenProvider.getAccessToken).not.toHaveBeenCalled();
    expect(fetchLike).not.toHaveBeenCalled();
  });

  it("fails fast for decryptEnvelopeKey when kms key is not configured", async () => {
    const tokenProvider = {
      getAccessToken: vi.fn(),
    };
    const fetchLike = vi.fn();

    const service = new KmsEnvelopeService(
      tokenProvider as any,
      "",
      fetchLike as any
    );

    await expect(
      service.decryptEnvelopeKey("wrapped-key", "uid:u1|session:s1|doc:passport")
    ).rejects.toThrow("IDENTITY_UPLOAD_KMS_KEY_NAME is not configured");
    expect(tokenProvider.getAccessToken).not.toHaveBeenCalled();
    expect(fetchLike).not.toHaveBeenCalled();
  });
});
