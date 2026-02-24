import { describe, expect, it } from "vitest";
import { classifyIdentityReviewFailure } from "./processIdentityUploadReview.js";
describe("classifyIdentityReviewFailure", () => {
    it("maps hash mismatch to payload_hash_mismatch", () => {
        expect(classifyIdentityReviewFailure("Payload sha256 does not match upload session")).toBe("payload_hash_mismatch");
    });
    it("maps schema errors to unsupported_encryption_schema", () => {
        expect(classifyIdentityReviewFailure("Unsupported payload encryption schema version")).toBe("unsupported_encryption_schema");
    });
    it("returns fallback value for unknown errors", () => {
        expect(classifyIdentityReviewFailure("Something unexpected")).toBe("review_processing_failed");
    });
});
//# sourceMappingURL=processIdentityUploadReview.test.js.map