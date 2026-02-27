import { describe, expect, it, vi } from "vitest";

vi.mock("@google-cloud/vision", () => ({
  ImageAnnotatorClient: class {},
}));

vi.mock("@simplewebauthn/server", () => ({
  generateAuthenticationOptions: vi.fn(),
  generateRegistrationOptions: vi.fn(),
  verifyAuthenticationResponse: vi.fn(),
  verifyRegistrationResponse: vi.fn(),
}));
import { classifyIdentityReviewFailure } from "../../workers/processIdentityUploadReview.js";

describe("classifyIdentityReviewFailure", () => {
  it("maps hash mismatch to payload_hash_mismatch", () => {
    expect(
      classifyIdentityReviewFailure("Payload sha256 does not match upload session")
    ).toBe("payload_hash_mismatch");
  });

  it("maps schema errors to unsupported_encryption_schema", () => {
    expect(
      classifyIdentityReviewFailure("Unsupported payload encryption schema version")
    ).toBe("unsupported_encryption_schema");
  });

  it("maps name mismatch failures", () => {
    expect(
      classifyIdentityReviewFailure("Name mismatch between client profile and extracted document name")
    ).toBe("name_mismatch");
  });

  it("maps missing extraction failures", () => {
    expect(
      classifyIdentityReviewFailure("Name extraction missing for identity review")
    ).toBe("name_extraction_missing");
  });

  it("maps OCR failures", () => {
    expect(
      classifyIdentityReviewFailure("Identity OCR failed: Vision OCR returned empty text")
    ).toBe("ocr_processing_failed");
  });

  it("returns fallback value for unknown errors", () => {
    expect(classifyIdentityReviewFailure("Something unexpected")).toBe(
      "review_processing_failed"
    );
  });
});
