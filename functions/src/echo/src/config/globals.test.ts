import { describe, expect, it } from "vitest";
import { APP } from "./globals.js";

describe("echo globals", () => {
  it("uses europe-west2 region", () => {
    expect(APP.region).toBe("europe-west2");
  });

  it("exposes boolean flags", () => {
    expect(typeof APP.logRequests).toBe("boolean");
    expect(typeof APP.emulator).toBe("boolean");
  });
});
