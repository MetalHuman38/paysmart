import { describe, expect, it } from "vitest";
import {
  evaluateNameMatch,
  IDENTITY_UPLOAD_PAYLOAD_CONTRACT_VERSION,
  parseIdentityUploadPayload,
} from "../../services/identityUploadPayloadContract.js";

function buildPayload(header: Record<string, unknown>, body: Buffer): Buffer {
  const headerBytes = Buffer.from(JSON.stringify(header), "utf8");
  const length = Buffer.alloc(4);
  length.writeUInt32BE(headerBytes.length, 0);
  return Buffer.concat([length, headerBytes, body]);
}

describe("identityUploadPayloadContract", () => {
  it("parses v2 contract payload with client information", () => {
    const payload = buildPayload(
      {
        contractVersion: IDENTITY_UPLOAD_PAYLOAD_CONTRACT_VERSION,
        document: {
          documentType: "passport",
          contentType: "image/jpeg",
        },
        clientInfo: {
          firstName: "Ada",
          middleName: "Lovelace",
          lastName: "Byron",
          fullName: "Ada Lovelace Byron",
          email: "ada@example.com",
          dateOfBirth: "1990-01-01",
          countryIso2: "GB",
        },
        extraction: {
          candidateFullName: "Ada Lovelace Byron",
          provider: "on_device_ocr_placeholder_v1",
        },
      },
      Buffer.from([1, 2, 3, 4])
    );

    const parsed = parseIdentityUploadPayload(payload);
    expect(parsed.contractVersion).toBe(IDENTITY_UPLOAD_PAYLOAD_CONTRACT_VERSION);
    expect(parsed.clientInfo.fullName).toBe("Ada Lovelace Byron");
    expect(parsed.extraction.provider).toBe("on_device_ocr_placeholder_v1");
    expect(parsed.documentBytes.byteLength).toBe(4);
  });

  it("rejects malformed payload headers", () => {
    const malformed = Buffer.from([0, 0, 0, 99, 1, 2, 3]);
    expect(() => parseIdentityUploadPayload(malformed)).toThrow(
      "Client payload header is malformed"
    );
  });

  it("evaluates likely name matches", () => {
    const match = evaluateNameMatch("Ada Lovelace Byron", "Ada Byron");
    const mismatch = evaluateNameMatch("Ada Lovelace Byron", "Grace Hopper");
    expect(match.isMatch).toBe(true);
    expect(mismatch.isMatch).toBe(false);
  });
});
