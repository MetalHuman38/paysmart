import { Request, Response } from "express";
import { authContainer } from "../infrastructure/di/authContainer.js";
import { HandleFlutterwaveWebhook } from "../application/usecase/HandleFlutterwaveWebhook.js";

export async function flutterwaveWebhookHandler(req: Request, res: Response) {
  try {
    const rawPayload = resolveRawPayload(req.body);
    if (!rawPayload) {
      return res.status(400).json({ error: "Missing payload" });
    }

    const signature = resolveSignatureHeader(req.headers);
    const { addMoneyFlutterwave } = authContainer();
    const useCase = new HandleFlutterwaveWebhook(addMoneyFlutterwave);
    const result = await useCase.execute(
      rawPayload,
      signature?.value,
      signature?.name
    );

    return res.status(200).json({
      ok: true,
      handled: result.handled,
      sessionId: result.sessionId ?? null,
      uid: result.uid ?? null,
      status: result.status ?? null,
      eventType: result.eventType ?? null,
    });
  } catch (error) {
    const message = error instanceof Error ? error.message : "Internal error";

    if (
      message.includes("signature") ||
      message.includes("payload") ||
      message.includes("Missing Flutterwave signature header")
    ) {
      return res.status(400).json({ error: message });
    }

    if (
      message.includes("not configured") ||
      message.includes("FLUTTERWAVE_WEBHOOK_SECRET_HASH is required")
    ) {
      return res.status(503).json({
        error: "Payments service is not configured",
        code: "PAYMENTS_SERVICE_MISCONFIGURED",
      });
    }

    console.error("flutterwaveWebhookHandler failed", error);
    return res.status(500).json({ error: "Internal error" });
  }
}

function resolveRawPayload(rawBody: unknown): string {
  if (Buffer.isBuffer(rawBody)) {
    return rawBody.toString("utf8");
  }
  if (typeof rawBody === "string") {
    return rawBody;
  }
  if (rawBody && typeof rawBody === "object") {
    return JSON.stringify(rawBody);
  }
  return "";
}

function resolveSignatureHeader(
  headers: Request["headers"]
): { name: string; value: string } | undefined {
  const flutterwaveSignature = headers["flutterwave-signature"];
  if (typeof flutterwaveSignature === "string" && flutterwaveSignature.trim()) {
    return {
      name: "flutterwave-signature",
      value: flutterwaveSignature.trim(),
    };
  }

  const verifHash = headers["verif-hash"];
  if (typeof verifHash === "string" && verifHash.trim()) {
    return {
      name: "verif-hash",
      value: verifHash.trim(),
    };
  }

  return undefined;
}
