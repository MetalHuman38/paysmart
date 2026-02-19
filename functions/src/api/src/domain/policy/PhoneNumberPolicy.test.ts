import { describe, expect, it } from "vitest";
import { normalizePhone, validateE164 } from "./PhoneNumberPolicy.js";

describe("PhoneNumberPolicy", () => {
  it("normalizes phone input by removing spaces and punctuation", () => {
    expect(normalizePhone("+1 (415) 555-1234")).toBe("+14155551234");
  });

  it("rejects invalid E.164 numbers", () => {
    expect(() => validateE164("4155551234")).toThrow(
      "Phone number must be in E.164 format"
    );
  });

  it("accepts valid E.164 numbers", () => {
    expect(() => validateE164("+14155551234")).not.toThrow();
  });
});
