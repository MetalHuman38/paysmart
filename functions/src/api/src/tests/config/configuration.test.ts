import { afterEach, describe, expect, it } from "vitest";
import { loadConfig } from "../../config/configuration.js";

const ORIGINAL_ENV = { ...process.env };

afterEach(() => {
  process.env = { ...ORIGINAL_ENV };
});

describe("loadConfig", () => {
  it("uses GCLOUD_PROJECT when GOOGLE_CLOUD_PROJECT is missing", () => {
    process.env = {
      ...ORIGINAL_ENV,
      GOOGLE_CLOUD_PROJECT: "",
      GCLOUD_PROJECT: "paysmart-7ee79",
      GCP_PROJECT: "",
      FIREBASE_PROJECT: "",
      FIREBASE_CONFIG: "",
      STORAGE_BUCKET: "",
      FIREBASE_STORAGE_BUCKET: "",
    };

    const config = loadConfig();

    expect(config.projectId).toBe("paysmart-7ee79");
    expect(config.storageBucket).toBe("paysmart-7ee79.appspot.com");
  });

  it("uses FIREBASE_CONFIG projectId/storageBucket when direct env is missing", () => {
    process.env = {
      ...ORIGINAL_ENV,
      GOOGLE_CLOUD_PROJECT: "",
      GCLOUD_PROJECT: "",
      GCP_PROJECT: "",
      FIREBASE_PROJECT: "",
      STORAGE_BUCKET: "",
      FIREBASE_STORAGE_BUCKET: "",
      FIREBASE_CONFIG: JSON.stringify({
        projectId: "paysmart-7ee79",
        storageBucket: "paysmart-7ee79.firebasestorage.app",
      }),
    };

    const config = loadConfig();

    expect(config.projectId).toBe("paysmart-7ee79");
    expect(config.storageBucket).toBe("paysmart-7ee79.firebasestorage.app");
  });

  it("builds passkey expected origins from web + android env values", () => {
    process.env = {
      ...ORIGINAL_ENV,
      PASSKEY_RP_ID: "pay-smart.net",
      PASSKEY_EXPECTED_ORIGINS: "https://pay-smart.net/,https://www.pay-smart.net",
      PASSKEY_ANDROID_APK_KEY_HASHES: "abc123==,android:apk-key-hash:ab+c/==",
      PASSKEY_ANDROID_EXPECTED_ORIGINS: "android:apk-key-hash:ghi789==",
    };

    const config = loadConfig();
    const origins = config.passkeyExpectedOrigins;

    expect(config.passkeyEnabled).toBe(true);
    expect(origins.has("https://pay-smart.net")).toBe(true);
    expect(origins.has("https://www.pay-smart.net")).toBe(true);
    expect(origins.has("android:apk-key-hash:abc123")).toBe(true);
    expect(origins.has("android:apk-key-hash:ab-c_")).toBe(true);
    expect(origins.has("android:apk-key-hash:ghi789")).toBe(true);
  });
});
