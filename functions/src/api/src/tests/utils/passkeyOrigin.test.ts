import { describe, expect, it } from "vitest";
import {
  normalizeAndroidPasskeyOrigin,
  normalizePasskeyOrigin,
} from "../../utils/passkeyOrigin.js";

describe("passkey origin normalization", () => {
  it("normalizes android hash to base64url without padding", () => {
    expect(normalizeAndroidPasskeyOrigin("android:apk-key-hash:ab+c/==")).toBe(
      "android:apk-key-hash:ab-c_"
    );
    expect(normalizeAndroidPasskeyOrigin("abc123==")).toBe(
      "android:apk-key-hash:abc123"
    );
  });

  it("normalizes web origins by trimming trailing slash", () => {
    expect(normalizePasskeyOrigin("https://www.pay-smart.net/")).toBe(
      "https://www.pay-smart.net"
    );
    expect(normalizePasskeyOrigin("https://pay-smart.net")).toBe(
      "https://pay-smart.net"
    );
  });
});

