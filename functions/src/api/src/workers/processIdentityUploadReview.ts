import { createHash } from "crypto";
import { FieldValue } from "firebase-admin/firestore";
import { getStorage } from "firebase-admin/storage";
import { onDocumentUpdated } from "firebase-functions/v2/firestore";
import { APP } from "../config/globals.js";
import { initDeps } from "../dependencies.js";
import { authContainer } from "../infrastructure/di/authContainer.js";
import { decryptIdentityPayload } from "../services/identityPayloadCrypto.js";
import {
  evaluateNameMatch,
  type ParsedIdentityUploadPayload,
  parseIdentityUploadPayload,
} from "../services/identityUploadPayloadContract.js";
import { GoogleVisionTextExtractionService } from "../services/googleVisionTextExtractionService.js";
import { GoogleCloudAccessTokenProvider } from "../services/googleCloudAccessTokenProvider.js";
import { KmsEnvelopeService } from "../services/kmsEnvelopeService.js";

type IdentityUploadSessionDoc = {
  status: string;
  objectPath: string;
  associatedData: string;
  payloadSha256: string;
  wrappedEnvelopeKeyBase64: string;
  verificationId?: string;
};

export const processIdentityUploadReview = onDocumentUpdated(
  {
    region: APP.region,
    document: "users/{uid}/identityUploads/{sessionId}",
    retry: false,
    memory: "512MiB",
    timeoutSeconds: 120,
  },
  async (event) => {
    const before = event.data?.before.data() as IdentityUploadSessionDoc | undefined;
    const after = event.data?.after.data() as IdentityUploadSessionDoc | undefined;

    if (!after) return;
    if (after.status !== "pending_review") return;
    if (before?.status === "pending_review") return;

    const uid = event.params.uid as string;
    const sessionId = event.params.sessionId as string;
    await processIdentityUploadReviewJob(uid, sessionId);
  }
);

export async function processIdentityUploadReviewJob(uid: string, sessionId: string) {
  const { firestore, getConfig } = initDeps();
  const config = getConfig();
  const bucketName = config.storageBucket || `${config.projectId}.appspot.com`;
  const uploadRef = firestore
    .collection("users")
    .doc(uid)
    .collection("identityUploads")
    .doc(sessionId);

  const uploadDoc = await firestore.runTransaction(async (tx) => {
    const snap = await tx.get(uploadRef);
    if (!snap.exists) return null;

    const data = snap.data() as IdentityUploadSessionDoc;
    if (data.status !== "pending_review") return null;

    tx.update(uploadRef, {
      status: "review_processing",
      reviewProcessingStartedAt: FieldValue.serverTimestamp(),
      updatedAt: FieldValue.serverTimestamp(),
    });

    return data;
  });

  if (!uploadDoc) {
    return;
  }

  try {
    const decryptedPayload = await decryptUploadedPayload({
      bucketName,
      objectPath: uploadDoc.objectPath,
      associatedData: uploadDoc.associatedData,
      wrappedEnvelopeKeyBase64: uploadDoc.wrappedEnvelopeKeyBase64,
      maxPayloadBytes: config.identityMaxPayloadBytes,
      kmsKeyName: config.identityKmsKeyName,
    });

    const payloadSha256 = createHash("sha256")
      .update(decryptedPayload)
      .digest("base64url");
    if (payloadSha256 !== uploadDoc.payloadSha256) {
      throw new Error("Payload sha256 does not match upload session");
    }

    const parsedPayload = parseIdentityUploadPayload(decryptedPayload);
    const expectedFullName = parsedPayload.clientInfo.fullName;
    const nameExtraction = await resolveCandidateNameForReview(
      parsedPayload,
      config.identityOcrEnabled,
      config.identityOcrAllowPayloadFallback
    );
    const candidateFullName = nameExtraction.candidateFullName;

    const nameMatch = evaluateNameMatch(expectedFullName, candidateFullName);
    if (!nameMatch.isMatch) {
      throw new Error("Name mismatch between client profile and extracted document name");
    }

    const normalizedExpectedName = expectedFullName.trim().toLowerCase();
    const normalizedCandidateName = candidateFullName.trim().toLowerCase();

    await uploadRef.set(
      {
        status: "verified",
        reviewDecision: "verified",
        reviewDecisionReason: null,
        payloadSha256Verified: true,
        decryptedPayloadBytes: decryptedPayload.byteLength,
        clientInfoContractVersion: parsedPayload.contractVersion,
        clientInfoFullNameHash: createHash("sha256")
          .update(normalizedExpectedName)
          .digest("hex"),
        extractedNameHash: createHash("sha256")
          .update(normalizedCandidateName)
          .digest("hex"),
        extractedNameProvider: nameExtraction.provider,
        extractedNameSource:
          nameExtraction.source === "vision" ?
            "google_vision_ocr" :
            "payload_fallback",
        nameMatchScore: nameMatch.score,
        reviewedAt: FieldValue.serverTimestamp(),
        updatedAt: FieldValue.serverTimestamp(),
      },
      { merge: true }
    );

    const { securitySettings } = authContainer();
    await securitySettings.createIfMissing(uid);
    await securitySettings.update(uid, {
      hasVerifiedIdentity: true,
      kycStatus: "verified",
      updatedAt: FieldValue.serverTimestamp() as any,
    });
  } catch (error) {
    const message = error instanceof Error ? error.message : "Identity review failed";
    const reason = classifyIdentityReviewFailure(message);

    await uploadRef.set(
      {
        status: "rejected",
        reviewDecision: "rejected",
        reviewDecisionReason: reason,
        reviewErrorMessage: message.slice(0, 512),
        reviewedAt: FieldValue.serverTimestamp(),
        updatedAt: FieldValue.serverTimestamp(),
      },
      { merge: true }
    );

    const { securitySettings } = authContainer();
    await securitySettings.createIfMissing(uid);
    await securitySettings.update(uid, {
      hasVerifiedIdentity: false,
      kycStatus: "rejected",
      updatedAt: FieldValue.serverTimestamp() as any,
    });
  }
}

type DecryptJobInput = {
  bucketName: string;
  objectPath: string;
  associatedData: string;
  wrappedEnvelopeKeyBase64: string;
  maxPayloadBytes: number;
  kmsKeyName: string;
};

async function decryptUploadedPayload(input: DecryptJobInput): Promise<Buffer> {
  if (!input.objectPath) {
    throw new Error("Missing upload object path");
  }
  if (!input.associatedData) {
    throw new Error("Missing upload associatedData");
  }
  if (!input.wrappedEnvelopeKeyBase64) {
    throw new Error("Missing wrapped envelope key");
  }

  const file = getStorage().bucket(input.bucketName).file(input.objectPath);
  const [exists] = await file.exists();
  if (!exists) {
    throw new Error("Encrypted identity payload file does not exist");
  }

  const [payloadBytes] = await file.download();
  if (!payloadBytes || payloadBytes.byteLength === 0) {
    throw new Error("Encrypted identity payload is empty");
  }

  if (payloadBytes.byteLength > input.maxPayloadBytes) {
    throw new Error("Encrypted identity payload exceeds max size");
  }

  const tokenProvider = new GoogleCloudAccessTokenProvider();
  const kmsEnvelopeService = new KmsEnvelopeService(
    tokenProvider,
    input.kmsKeyName
  );
  const decryptedEnvelopeKey = await kmsEnvelopeService.decryptEnvelopeKey(
    input.wrappedEnvelopeKeyBase64,
    input.associatedData
  );

  return decryptIdentityPayload(
    Buffer.from(payloadBytes),
    decryptedEnvelopeKey,
    input.associatedData
  );
}

export function classifyIdentityReviewFailure(message: string): string {
  const value = message.toLowerCase();
  if (value.includes("legacy identity payload")) return "legacy_payload_contract";
  if (value.includes("client payload")) return "payload_contract_invalid";
  if (value.includes("name extraction missing")) return "name_extraction_missing";
  if (value.includes("identity ocr") || value.includes("vision ocr")) {
    return "ocr_processing_failed";
  }
  if (value.includes("name mismatch")) return "name_mismatch";
  if (value.includes("client info")) return "client_info_missing";
  if (value.includes("schema")) return "unsupported_encryption_schema";
  if (value.includes("sha256") || value.includes("hash")) return "payload_hash_mismatch";
  if (value.includes("auth") || value.includes("integrity")) return "payload_integrity_failed";
  if (value.includes("kms")) return "kms_decrypt_failed";
  if (value.includes("exceeds")) return "payload_too_large";
  if (value.includes("empty")) return "payload_empty";
  if (value.includes("does not exist")) return "payload_missing";
  return "review_processing_failed";
}

type ReviewNameExtraction = {
  candidateFullName: string;
  provider: string;
  source: "vision" | "payload";
};

async function resolveCandidateNameForReview(
  parsedPayload: ParsedIdentityUploadPayload,
  ocrEnabled: boolean,
  allowPayloadFallback: boolean
): Promise<ReviewNameExtraction> {
  const payloadCandidate = parsedPayload.extraction.candidateFullName?.trim();
  const payloadProvider = parsedPayload.extraction.provider?.trim();

  let visionError: Error | undefined;
  if (ocrEnabled) {
    try {
      const vision = new GoogleVisionTextExtractionService();
      const extraction = await vision.extract(
        parsedPayload.documentBytes,
        parsedPayload.contentType
      );
      const visionCandidate = extraction.candidateFullName?.trim();
      if (visionCandidate) {
        return {
          candidateFullName: visionCandidate,
          provider: extraction.provider,
          source: "vision",
        };
      }
    } catch (error) {
      if (error instanceof Error) {
        visionError = error;
      } else {
        visionError = new Error("Unknown Vision OCR failure");
      }
    }
  }

  if (allowPayloadFallback && payloadCandidate) {
    return {
      candidateFullName: payloadCandidate,
      provider: payloadProvider || "client_payload_extraction",
      source: "payload",
    };
  }

  if (visionError) {
    throw new Error(`Identity OCR failed: ${visionError.message}`);
  }
  throw new Error("Name extraction missing for identity review");
}
