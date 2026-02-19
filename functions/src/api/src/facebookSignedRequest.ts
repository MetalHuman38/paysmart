import crypto from "crypto";

export type FacebookSignedRequestPayload = {
  algorithm?: string;
  user_id?: string;
  issued_at?: number;
  [key: string]: unknown;
};

function base64UrlDecode(input: string): Buffer {
  const normalized = input.replace(/-/g, "+").replace(/_/g, "/");
  const padding = "=".repeat((4 - (normalized.length % 4)) % 4);
  return Buffer.from(normalized + padding, "base64");
}

export function parseSignedRequest(
  signedRequest: string,
  appSecret: string
): FacebookSignedRequestPayload {
  const [encodedSig, encodedPayload] = signedRequest.split(".");
  if (!encodedSig || !encodedPayload) {
    throw new Error("signed_request must be 'signature.payload'");
  }

  const rawSig = base64UrlDecode(encodedSig);
  let payloadJson: string;
  try {
    payloadJson = base64UrlDecode(encodedPayload).toString("utf8");
  } catch (err) {
    throw new Error("payload is not valid base64");
  }

  let payload: FacebookSignedRequestPayload;
  try {
    payload = JSON.parse(payloadJson);
  } catch (err) {
    throw new Error("payload is not valid JSON");
  }

  const algorithm = String(payload.algorithm || "").toUpperCase();
  if (algorithm !== "HMAC-SHA256") {
    throw new Error(`unexpected algorithm '${algorithm || "missing"}'`);
  }

  const expectedSig = crypto
    .createHmac("sha256", appSecret)
    .update(encodedPayload)
    .digest();

  if (
    rawSig.length !== expectedSig.length ||
    !crypto.timingSafeEqual(rawSig, expectedSig)
  ) {
    throw new Error("signature verification failed");
  }

  return payload;
}
