import { randomBytes } from "crypto";
import { describe, expect, it } from "vitest";
import {
  decryptIdentityPayload,
  encryptIdentityPayload,
} from "./identityPayloadCrypto.js";

describe("identityPayloadCrypto", () => {
  it("round-trips payload encryption/decryption", () => {
    const key = randomBytes(32);
    const plainText = Buffer.from("sample-identity-binary-payload");
    const associatedData = "uid:user-1|session:session-1|doc:passport";

    const encrypted = encryptIdentityPayload(plainText, key, associatedData);
    const decrypted = decryptIdentityPayload(encrypted, key, associatedData);

    expect(decrypted.equals(plainText)).toBe(true);
  });

  it("rejects payload when associated data does not match", () => {
    const key = randomBytes(32);
    const plainText = Buffer.from("sample-identity-binary-payload");
    const encrypted = encryptIdentityPayload(
      plainText,
      key,
      "uid:user-1|session:session-1|doc:passport"
    );

    expect(() =>
      decryptIdentityPayload(
        encrypted,
        key,
        "uid:user-1|session:session-1|doc:national_id"
      )
    ).toThrow();
  });
});
