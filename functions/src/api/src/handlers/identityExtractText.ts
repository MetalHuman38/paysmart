import { Request, Response } from "express";
import { initDeps } from "../dependencies.js";
import { authContainer } from "../infrastructure/di/authContainer.js";

function parsePayloadBase64(payloadBase64: string): Buffer {
  try {
    return Buffer.from(payloadBase64, "base64");
  } catch {
    throw new Error("Invalid payloadBase64");
  }
}

export async function identityExtractTextHandler(req: Request, res: Response) {
  try {
    const authHeader = req.headers.authorization;
    if (!authHeader?.startsWith("Bearer ")) {
      return res.status(401).json({ error: "Missing token" });
    }

    const mimeType = (req.body?.mimeType ?? "").toString().trim();
    const payloadBase64 = (req.body?.payloadBase64 ?? "").toString().trim();
    if (!mimeType) {
      return res.status(400).json({ error: "Missing mimeType" });
    }
    if (!payloadBase64) {
      return res.status(400).json({ error: "Missing payloadBase64" });
    }

    const payload = parsePayloadBase64(payloadBase64);
    if (payload.byteLength === 0) {
      return res.status(400).json({ error: "Decoded payload is empty" });
    }

    const { getConfig, auth } = initDeps();
    const config = getConfig();
    if (payload.byteLength > config.identityMaxPayloadBytes) {
      return res.status(413).json({ error: "Payload exceeds max allowed size" });
    }

    const idToken = authHeader.split("Bearer ")[1];
    await auth.verifyIdToken(idToken);

    const { identityTextExtraction } = authContainer();
    const extraction = await identityTextExtraction.extract(payload, mimeType);
    return res.status(200).json(extraction);
  } catch (error) {
    const message = error instanceof Error ? error.message : "Internal error";
    if (
      message.includes("Missing") ||
      message.includes("Invalid") ||
      message.includes("empty")
    ) {
      return res.status(400).json({ error: message });
    }
    if (message.includes("IDENTITY_OCR_NOT_CONFIGURED")) {
      return res.status(503).json({
        error: "Identity OCR service is not configured",
        code: "IDENTITY_OCR_NOT_CONFIGURED",
      });
    }

    console.error("identityExtractTextHandler failed", error);
    return res.status(500).json({ error: "Internal error" });
  }
}
