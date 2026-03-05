import { Request, Response } from "express";
import { initDeps } from "../dependencies.js";
import { authContainer } from "../infrastructure/di/authContainer.js";
import { CommitIdentityUpload } from "../application/usecase/CommitIdentityUpload.js";

export async function identityUploadCommitHandler(req: Request, res: Response) {
  try {
    const authHeader = req.headers.authorization;
    if (!authHeader?.startsWith("Bearer ")) {
      return res.status(401).json({ error: "Missing token" });
    }

    const sessionId = (req.body?.sessionId ?? "").toString().trim();
    const payloadSha256 = (req.body?.payloadSha256 ?? "").toString().trim();
    const attestationJwt = (req.body?.attestationJwt ?? "").toString().trim();

    if (!sessionId) {
      return res.status(400).json({ error: "Missing sessionId" });
    }

    if (!payloadSha256) {
      return res.status(400).json({ error: "Missing payloadSha256" });
    }

    if (!attestationJwt) {
      return res.status(400).json({ error: "Missing attestationJwt" });
    }

    const idToken = authHeader.split("Bearer ")[1];
    const { auth } = initDeps();
    const decoded = await auth.verifyIdToken(idToken);

    const { identityUploads, securitySettings } = authContainer();
    const useCase = new CommitIdentityUpload(identityUploads, securitySettings);
    const receipt = await useCase.execute(decoded.uid, {
      sessionId,
      payloadSha256,
      attestationJwt,
    });

    return res.status(200).json(receipt);
  } catch (error) {
    const message = error instanceof Error ? error.message : "Internal error";
    const classified = classifyIdentityCommitError(message);
    if (classified) {
      return res.status(classified.status).json({
        error: message,
        code: classified.code,
      });
    }

    if (
      message.includes("expired") ||
      message.includes("mismatch") ||
      message.includes("not found") ||
      message.includes("Missing") ||
      message.includes("uploaded") ||
      message.includes("empty") ||
      message.includes("attestation") ||
      message.includes("Integrity") ||
      message.includes("payload") ||
      message.includes("session") ||
      message.includes("disabled") ||
      message.includes("licensed")
    ) {
      return res.status(400).json({ error: message });
    }

    console.error("identityUploadCommitHandler failed", error);
    return res.status(500).json({ error: "Internal error" });
  }
}

function classifyIdentityCommitError(
  message: string
): { status: number; code: string } | null {
  if (message.includes("Fallback attestation token is disabled")) {
    return { status: 400, code: "ATTESTATION_FALLBACK_DISABLED" };
  }
  if (message.includes("PLAY_INTEGRITY_PACKAGE_NAME is not configured")) {
    return { status: 503, code: "PLAY_INTEGRITY_NOT_CONFIGURED" };
  }
  if (message.includes("Play Integrity decode failed")) {
    return { status: 502, code: "PLAY_INTEGRITY_DECODE_FAILED" };
  }
  if (message.includes("Play Integrity nonce mismatch")) {
    return { status: 400, code: "PLAY_INTEGRITY_NONCE_MISMATCH" };
  }
  if (message.includes("Play Integrity package mismatch")) {
    return { status: 400, code: "PLAY_INTEGRITY_PACKAGE_MISMATCH" };
  }
  if (message.includes("Play Integrity app verdict is not allowed")) {
    return { status: 400, code: "PLAY_INTEGRITY_APP_VERDICT_REJECTED" };
  }
  if (message.includes("Play Integrity device verdict is not allowed")) {
    return { status: 400, code: "PLAY_INTEGRITY_DEVICE_VERDICT_REJECTED" };
  }
  if (message.includes("Play Integrity app licensing verdict is not licensed")) {
    return { status: 400, code: "PLAY_INTEGRITY_NOT_LICENSED" };
  }
  return null;
}
