import { createCipheriv, createDecipheriv, randomBytes } from "crypto";
export const IDENTITY_PAYLOAD_SCHEMA_VERSION = 1;
const AES_GCM_IV_LENGTH = 12;
const AES_GCM_TAG_LENGTH = 16;
const AES_256_GCM_KEY_LENGTH = 32;
export function encryptIdentityPayload(plainBytes, key, associatedData) {
    validateKeyLength(key);
    const iv = randomBytes(AES_GCM_IV_LENGTH);
    const cipher = createCipheriv("aes-256-gcm", key, iv);
    cipher.setAAD(Buffer.from(associatedData, "utf8"));
    const encrypted = Buffer.concat([cipher.update(plainBytes), cipher.final()]);
    const tag = cipher.getAuthTag();
    return Buffer.concat([
        Buffer.from([IDENTITY_PAYLOAD_SCHEMA_VERSION]),
        iv,
        encrypted,
        tag,
    ]);
}
export function decryptIdentityPayload(encryptedBytes, key, associatedData) {
    validateKeyLength(key);
    if (encryptedBytes.length < 1 + AES_GCM_IV_LENGTH + AES_GCM_TAG_LENGTH + 1) {
        throw new Error("Encrypted payload is too small");
    }
    const version = encryptedBytes[0];
    if (version !== IDENTITY_PAYLOAD_SCHEMA_VERSION) {
        throw new Error("Unsupported payload encryption schema version");
    }
    const ivOffset = 1;
    const bodyOffset = ivOffset + AES_GCM_IV_LENGTH;
    const iv = encryptedBytes.subarray(ivOffset, bodyOffset);
    const body = encryptedBytes.subarray(bodyOffset);
    if (body.length <= AES_GCM_TAG_LENGTH) {
        throw new Error("Encrypted payload body is malformed");
    }
    const cipherText = body.subarray(0, body.length - AES_GCM_TAG_LENGTH);
    const authTag = body.subarray(body.length - AES_GCM_TAG_LENGTH);
    const decipher = createDecipheriv("aes-256-gcm", key, iv);
    decipher.setAAD(Buffer.from(associatedData, "utf8"));
    decipher.setAuthTag(authTag);
    return Buffer.concat([decipher.update(cipherText), decipher.final()]);
}
function validateKeyLength(key) {
    if (key.length !== AES_256_GCM_KEY_LENGTH) {
        throw new Error("Envelope key must be 32 bytes");
    }
}
//# sourceMappingURL=identityPayloadCrypto.js.map