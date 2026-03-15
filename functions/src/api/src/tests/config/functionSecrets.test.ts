import { describe, expect, it } from "vitest";
import { API_FUNCTION_SECRETS } from "../../config/functionSecrets.js";

describe("API_FUNCTION_SECRETS", () => {
  it("binds the Flutterwave provider auth secrets", () => {
    expect(API_FUNCTION_SECRETS).toEqual(
      expect.arrayContaining([
        "FLUTTERWAVE_SECRET_KEY",
        "FLUTTERWAVE_CLIENT_ID",
        "FLUTTERWAVE_CLIENT_SECRET",
      ])
    );
  });

  it("does not include the legacy mistyped Flutterwave OAuth secret names", () => {
    expect(API_FUNCTION_SECRETS).not.toContain("FLUTTER_WAVE_CLIENT_ID");
    expect(API_FUNCTION_SECRETS).not.toContain("FLUTTER_WAVE_CLIENT_SECRET");
  });
});
