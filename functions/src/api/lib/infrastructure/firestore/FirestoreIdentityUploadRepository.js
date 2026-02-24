import { randomBytes, randomUUID } from "crypto";
import { FieldValue } from "firebase-admin/firestore";
import { getStorage } from "firebase-admin/storage";
import { IDENTITY_PAYLOAD_SCHEMA_VERSION } from "../../services/identityPayloadCrypto.js";
const SESSION_TTL_MS = 15 * 60 * 1000;
const ALLOWED_DOCUMENT_TYPES = new Set([
    "passport",
    "drivers_license",
    "national_id",
]);
const ALLOWED_CONTENT_TYPES = new Set([
    "image/jpeg",
    "image/jpg",
    "image/png",
    "application/pdf",
]);
const ENCRYPTED_UPLOAD_CONTENT_TYPE = "application/octet-stream";
const CRYPTO_CONTRACT_VERSION = `aes-256-gcm-v1/payload-v${IDENTITY_PAYLOAD_SCHEMA_VERSION}`;
export class FirestoreIdentityUploadRepository {
    firestore;
    storageBucketName;
    attestationVerifier;
    kmsEnvelopeService;
    maxPayloadBytes;
    constructor(firestore, storageBucketName, attestationVerifier, kmsEnvelopeService, maxPayloadBytes) {
        this.firestore = firestore;
        this.storageBucketName = storageBucketName;
        this.attestationVerifier = attestationVerifier;
        this.kmsEnvelopeService = kmsEnvelopeService;
        this.maxPayloadBytes = maxPayloadBytes;
    }
    sessionRef(uid, sessionId) {
        return this.firestore
            .collection("users")
            .doc(uid)
            .collection("identityUploads")
            .doc(sessionId);
    }
    ensureValidInput(input) {
        if (!ALLOWED_DOCUMENT_TYPES.has(input.documentType)) {
            throw new Error("Unsupported documentType");
        }
        const payloadSha256 = input.payloadSha256.trim();
        if (payloadSha256.length < 20 || payloadSha256.length > 128) {
            throw new Error("Invalid payloadSha256");
        }
        const contentType = input.contentType.trim().toLowerCase();
        if (!ALLOWED_CONTENT_TYPES.has(contentType)) {
            throw new Error("Unsupported contentType");
        }
    }
    async createSession(uid, input) {
        this.ensureValidInput(input);
        const sessionId = randomUUID();
        const expiresAtMs = Date.now() + SESSION_TTL_MS;
        const objectPath = `identityUploads/${uid}/${sessionId}/payload.enc`;
        const associatedData = `uid:${uid}|session:${sessionId}|doc:${input.documentType}`;
        const attestationNonce = randomBytes(24).toString("base64url");
        const envelope = await this.kmsEnvelopeService.createEnvelopeKey(associatedData);
        const file = getStorage().bucket(this.storageBucketName).file(objectPath);
        const storageEmulatorHost = process.env.FIREBASE_STORAGE_EMULATOR_HOST?.trim();
        const uploadUrl = storageEmulatorHost ?
            // Signed URL upload is not implemented by Storage emulator.
            // Client uses /auth/identity/upload/payload fallback endpoint in local mode.
            "" :
            (await file.getSignedUrl({
                version: "v4",
                action: "write",
                expires: expiresAtMs,
                contentType: ENCRYPTED_UPLOAD_CONTENT_TYPE,
            }))[0];
        const sessionDoc = {
            sessionId,
            uid,
            documentType: input.documentType,
            payloadSha256: input.payloadSha256.trim(),
            contentType: input.contentType.trim().toLowerCase(),
            objectPath,
            associatedData,
            attestationNonce,
            status: "session_created",
            expiresAtMs,
            wrappedEnvelopeKeyBase64: envelope.wrappedKeyBase64,
            kmsKeyName: envelope.kmsKeyName,
            encryptionSchema: envelope.encryptionSchema,
            cryptoContractVersion: CRYPTO_CONTRACT_VERSION,
        };
        await this.sessionRef(uid, sessionId).set({
            ...sessionDoc,
            createdAt: FieldValue.serverTimestamp(),
            updatedAt: FieldValue.serverTimestamp(),
        });
        return {
            sessionId,
            uploadUrl,
            objectPath,
            associatedData,
            attestationNonce,
            expiresAtMs,
            encryptionKeyBase64: envelope.encryptionKeyBase64,
            encryptionSchema: envelope.encryptionSchema,
            cryptoContractVersion: CRYPTO_CONTRACT_VERSION,
        };
    }
    async uploadEncryptedPayload(uid, input) {
        const storageEmulatorHost = process.env.FIREBASE_STORAGE_EMULATOR_HOST?.trim();
        if (!storageEmulatorHost) {
            throw new Error("EMULATOR_ONLY: direct payload upload endpoint is only available when FIREBASE_STORAGE_EMULATOR_HOST is configured");
        }
        const sessionId = input.sessionId.trim();
        if (!sessionId)
            throw new Error("Missing sessionId");
        const payloadBase64 = input.payloadBase64.trim();
        if (!payloadBase64)
            throw new Error("Missing payloadBase64");
        const encryptedPayload = decodeBase64Payload(payloadBase64);
        if (encryptedPayload.byteLength <= 0) {
            throw new Error("Encrypted payload is empty");
        }
        if (encryptedPayload.byteLength > this.maxPayloadBytes) {
            throw new Error("Encrypted payload exceeds max size");
        }
        const ref = this.sessionRef(uid, sessionId);
        const snap = await ref.get();
        if (!snap.exists) {
            throw new Error("Upload session not found");
        }
        const data = snap.data();
        if (data.status !== "session_created") {
            throw new Error("Upload session already committed");
        }
        if (Date.now() > data.expiresAtMs) {
            throw new Error("Upload session expired");
        }
        const objectPath = data.objectPath;
        const file = getStorage().bucket(this.storageBucketName).file(objectPath);
        await file.save(encryptedPayload, {
            resumable: false,
            contentType: ENCRYPTED_UPLOAD_CONTENT_TYPE,
            metadata: {
                metadata: {
                    uploadedVia: "emulator_fallback_endpoint",
                },
            },
        });
        await ref.set({
            payloadUploadedAt: FieldValue.serverTimestamp(),
            payloadUploadedVia: "emulator_fallback_endpoint",
            payloadUploadedBytes: encryptedPayload.byteLength,
            updatedAt: FieldValue.serverTimestamp(),
        }, { merge: true });
        return {
            sessionId,
            objectPath,
            bytesWritten: encryptedPayload.byteLength,
        };
    }
    async commitSession(uid, input) {
        const sessionId = input.sessionId.trim();
        if (!sessionId)
            throw new Error("Missing sessionId");
        const payloadSha256 = input.payloadSha256.trim();
        if (!payloadSha256)
            throw new Error("Missing payloadSha256");
        const attestationJwt = input.attestationJwt.trim();
        if (!attestationJwt)
            throw new Error("Missing attestationJwt");
        const ref = this.sessionRef(uid, sessionId);
        const snap = await ref.get();
        if (!snap.exists) {
            throw new Error("Upload session not found");
        }
        const data = snap.data();
        if (data.status === "pending_review" && data.verificationId) {
            return {
                verificationId: data.verificationId,
                status: "pending_review",
            };
        }
        if (data.status !== "session_created") {
            throw new Error("Upload session already committed");
        }
        if (Date.now() > data.expiresAtMs) {
            throw new Error("Upload session expired");
        }
        if (data.payloadSha256 !== payloadSha256) {
            throw new Error("payloadSha256 mismatch");
        }
        const file = getStorage().bucket(this.storageBucketName).file(data.objectPath);
        const [exists] = await file.exists();
        if (!exists) {
            throw new Error("Encrypted payload not uploaded");
        }
        const [metadata] = await file.getMetadata();
        const objectSizeBytes = Number(metadata.size ?? 0);
        if (!Number.isFinite(objectSizeBytes) || objectSizeBytes <= 0) {
            throw new Error("Encrypted payload is empty");
        }
        if (objectSizeBytes > this.maxPayloadBytes) {
            throw new Error("Encrypted payload exceeds max size");
        }
        const attestation = await this.attestationVerifier.verify(attestationJwt, data.attestationNonce);
        const verificationId = data.verificationId ?? sessionId;
        await ref.set({
            status: "pending_review",
            verificationId,
            objectGeneration: metadata.generation ?? null,
            objectSizeBytes,
            attestationProvider: attestation.provider,
            attestationPackageName: attestation.packageName,
            attestationTimestampMs: attestation.timestampMillis,
            attestationAppRecognitionVerdict: attestation.appRecognitionVerdict,
            attestationDeviceRecognitionVerdicts: attestation.deviceRecognitionVerdicts,
            attestationAppLicensingVerdict: attestation.appLicensingVerdict,
            attestationDigest: attestation.attestationDigest,
            committedAt: FieldValue.serverTimestamp(),
            updatedAt: FieldValue.serverTimestamp(),
        }, { merge: true });
        return {
            verificationId,
            status: "pending_review",
        };
    }
}
function decodeBase64Payload(raw) {
    const normalized = raw.replace(/-/g, "+").replace(/_/g, "/");
    try {
        const bytes = Buffer.from(normalized, "base64");
        if (bytes.byteLength === 0) {
            throw new Error("empty");
        }
        return bytes;
    }
    catch {
        throw new Error("Invalid payloadBase64");
    }
}
//# sourceMappingURL=FirestoreIdentityUploadRepository.js.map