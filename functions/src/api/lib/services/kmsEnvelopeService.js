import { randomBytes } from "crypto";
const CLOUD_PLATFORM_SCOPE = "https://www.googleapis.com/auth/cloud-platform";
const CLOUD_KMS_ENDPOINT = "https://cloudkms.googleapis.com/v1";
const ENCRYPTION_SCHEMA = "aes-256-gcm-v1";
export class KmsEnvelopeService {
    tokenProvider;
    kmsKeyName;
    fetchLike;
    constructor(tokenProvider, kmsKeyName, fetchLike = fetch) {
        this.tokenProvider = tokenProvider;
        this.kmsKeyName = kmsKeyName;
        this.fetchLike = fetchLike;
    }
    get schema() {
        return ENCRYPTION_SCHEMA;
    }
    async createEnvelopeKey(associatedData) {
        this.ensureConfigured();
        const plainKey = randomBytes(32);
        const additionalAuthenticatedData = Buffer.from(associatedData, "utf8").toString("base64");
        const accessToken = await this.tokenProvider.getAccessToken([
            CLOUD_PLATFORM_SCOPE,
        ]);
        const response = await this.fetchLike(`${CLOUD_KMS_ENDPOINT}/${this.kmsKeyName}:encrypt`, {
            method: "POST",
            headers: {
                Authorization: `Bearer ${accessToken}`,
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                plaintext: plainKey.toString("base64"),
                additionalAuthenticatedData,
            }),
        });
        if (!response.ok) {
            const errorBody = await response.text();
            throw new Error(`Cloud KMS encrypt failed (${response.status}): ${errorBody}`);
        }
        const payload = (await response.json());
        const wrappedKeyBase64 = payload.ciphertext;
        if (!wrappedKeyBase64) {
            throw new Error("Cloud KMS encrypt response is missing ciphertext");
        }
        return {
            encryptionKeyBase64: plainKey.toString("base64url"),
            wrappedKeyBase64,
            encryptionSchema: this.schema,
            kmsKeyName: this.kmsKeyName,
        };
    }
    async decryptEnvelopeKey(wrappedKeyBase64, associatedData) {
        this.ensureConfigured();
        if (!wrappedKeyBase64) {
            throw new Error("Missing wrapped envelope key");
        }
        const additionalAuthenticatedData = Buffer.from(associatedData, "utf8").toString("base64");
        const accessToken = await this.tokenProvider.getAccessToken([
            CLOUD_PLATFORM_SCOPE,
        ]);
        const response = await this.fetchLike(`${CLOUD_KMS_ENDPOINT}/${this.kmsKeyName}:decrypt`, {
            method: "POST",
            headers: {
                Authorization: `Bearer ${accessToken}`,
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                ciphertext: wrappedKeyBase64,
                additionalAuthenticatedData,
            }),
        });
        if (!response.ok) {
            const errorBody = await response.text();
            throw new Error(`Cloud KMS decrypt failed (${response.status}): ${errorBody}`);
        }
        const payload = (await response.json());
        if (!payload.plaintext) {
            throw new Error("Cloud KMS decrypt response is missing plaintext");
        }
        return Buffer.from(payload.plaintext, "base64");
    }
    ensureConfigured() {
        if (!this.kmsKeyName) {
            throw new Error("IDENTITY_UPLOAD_KMS_KEY_NAME is not configured");
        }
    }
}
//# sourceMappingURL=kmsEnvelopeService.js.map