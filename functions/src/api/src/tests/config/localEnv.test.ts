import { describe, expect, it } from "vitest";
import {
  buildLocalEnvPaths,
  resolveLocalEnvProjectId,
  shouldLoadLocalEnv,
} from "../../config/localEnv.js";

describe("localEnv", () => {
  it("resolves the Firebase project id from the environment", () => {
    expect(
      resolveLocalEnvProjectId({
        GOOGLE_CLOUD_PROJECT: "",
        GCLOUD_PROJECT: "paysmart-7ee79",
      })
    ).toBe("paysmart-7ee79");
  });

  it("builds package-root env paths including project-specific and local overrides", () => {
    expect(
      buildLocalEnvPaths("C:\\repo\\src\\api", {
        GOOGLE_CLOUD_PROJECT: "paysmart-7ee79",
      })
    ).toEqual([
      "C:\\repo\\src\\api\\.env",
      "C:\\repo\\src\\api\\.env.paysmart-7ee79",
      "C:\\repo\\src\\api\\.env.local",
    ]);
  });

  it("omits the project-specific file when no project id is available", () => {
    expect(buildLocalEnvPaths("C:\\repo\\src\\api", {})).toEqual([
      "C:\\repo\\src\\api\\.env",
      "C:\\repo\\src\\api\\.env.local",
    ]);
  });

  it("does not load local env files in hosted Cloud Run or Functions runtime", () => {
    expect(
      shouldLoadLocalEnv({
        K_SERVICE: "api",
        GOOGLE_CLOUD_PROJECT: "paysmart-7ee79",
      })
    ).toBe(false);
  });

  it("still loads local env files in emulator runtime", () => {
    expect(
      shouldLoadLocalEnv({
        K_SERVICE: "api",
        FUNCTIONS_EMULATOR: "true",
        GOOGLE_CLOUD_PROJECT: "paysmart-7ee79",
      })
    ).toBe(true);
  });
});
