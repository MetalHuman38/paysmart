import { Request, Response } from "express";
import { initDeps } from "../dependencies.js";
import { authContainer } from "../infrastructure/di/authContainer.js";
import { CreateFlutterwaveAddMoneySession } from "../application/usecase/CreateFlutterwaveAddMoneySession.js";

export async function createFlutterwaveAddMoneySessionHandler(
  req: Request,
  res: Response
) {
  try {
    const authHeader = req.headers.authorization;
    if (!authHeader?.startsWith("Bearer ")) {
      return res.status(401).json({ error: "Missing token" });
    }

    const idToken = authHeader.split("Bearer ")[1];
    const { auth } = initDeps();
    const decoded = await auth.verifyIdToken(idToken);

    const amountMinor = resolveAmountMinor(req.body);
    const currency = (req.body?.currency ?? "NGN").toString();
    const idempotencyKey = resolveIdempotencyKey(req);

    const { addMoneyFlutterwave } = authContainer();
    const useCase = new CreateFlutterwaveAddMoneySession(addMoneyFlutterwave);
    const session = await useCase.execute(decoded.uid, {
      amountMinor,
      currency,
      idempotencyKey,
    });

    return res.status(200).json(session);
  } catch (error) {
    const message = error instanceof Error ? error.message : "Internal error";
    const paymentsConfigErrorCode = resolvePaymentsConfigErrorCode(message);

    if (paymentsConfigErrorCode) {
      return res.status(503).json({
        error: "Payments service is not configured",
        code: paymentsConfigErrorCode,
      });
    }

    if (
      message.includes("Invalid") ||
      message.includes("Unsupported") ||
      message.includes("Missing") ||
      message.includes("Amount must be") ||
      message.includes("Amount exceeds")
    ) {
      return res.status(400).json({ error: message });
    }

    console.error("createFlutterwaveAddMoneySessionHandler failed", error);
    return res.status(500).json({ error: "Internal error" });
  }
}

function resolveAmountMinor(rawBody: unknown): number {
  const body = rawBody as Record<string, unknown> | undefined;
  const asMinor = body?.amountMinor;
  const parsedMinor = parseNumber(asMinor);
  if (parsedMinor !== null) {
    return parsedMinor;
  }

  const asMajor = parseNumber(body?.amount);
  if (asMajor === null) {
    throw new Error("Missing amountMinor");
  }
  return Math.round(asMajor * 100);
}

function resolveIdempotencyKey(req: Request): string | undefined {
  const fromHeader = req.headers["idempotency-key"];
  if (typeof fromHeader === "string" && fromHeader.trim()) {
    return fromHeader.trim();
  }

  const fromBody = (req.body as Record<string, unknown> | undefined)?.idempotencyKey;
  if (typeof fromBody === "string" && fromBody.trim()) {
    return fromBody.trim();
  }

  return undefined;
}

function parseNumber(raw: unknown): number | null {
  if (typeof raw === "number" && Number.isFinite(raw)) {
    return raw;
  }
  if (typeof raw === "string") {
    const parsed = Number(raw.trim());
    if (Number.isFinite(parsed)) {
      return parsed;
    }
  }
  return null;
}

function resolvePaymentsConfigErrorCode(message: string): string | null {
  if (message.includes("FLUTTERWAVE_SECRET_KEY is not configured")) {
    return "MISSING_FLUTTERWAVE_SECRET_KEY";
  }
  if (message.includes("FLUTTERWAVE_PUBLIC_KEY is not configured")) {
    return "MISSING_FLUTTERWAVE_PUBLIC_KEY";
  }
  if (message.includes("FLUTTERWAVE_NOT_IMPLEMENTED_FLW_001")) {
    return "FLUTTERWAVE_NOT_IMPLEMENTED";
  }
  return null;
}
