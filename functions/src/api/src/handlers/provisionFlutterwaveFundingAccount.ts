import { Request, Response } from "express";
import { initDeps } from "../dependencies.js";
import { ProvisionFlutterwaveFundingAccount } from "../application/usecase/ProvisionFlutterwaveFundingAccount.js";
import { authContainer } from "../infrastructure/di/authContainer.js";
import { resolveFlutterwavePaymentsConfigErrorCode } from "./utils/flutterwavePaymentsConfigError.js";
import { FlutterwaveProviderRequestError } from "../services/flutterwavePaymentsService.js";

export async function provisionFlutterwaveFundingAccountHandler(
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

    const { flutterwaveFundingAccounts } = authContainer();
    const useCase = new ProvisionFlutterwaveFundingAccount(flutterwaveFundingAccounts);
    const result = await useCase.execute(decoded.uid, {
      idempotencyKey: resolveIdempotencyKey(req),
      kyc: resolveFundingAccountKyc(req.body),
    });

    return res.status(result.provisioningResult === "created" ? 201 : 200).json(result);
  } catch (error) {
    const message = error instanceof Error ? error.message : "Internal error";

    if (message === "FLUTTERWAVE_FUNDING_ACCOUNT_KYC_REQUIRED") {
      return res.status(422).json({
        error: "Verified BVN or NIN is required before provisioning a permanent NGN funding account",
        code: "FLUTTERWAVE_FUNDING_ACCOUNT_KYC_REQUIRED",
      });
    }

    if (error instanceof FlutterwaveProviderRequestError) {
      if (error.status === 409) {
        return res.status(409).json({
          error: "Flutterwave funding account conflict",
          code: "FLUTTERWAVE_FUNDING_ACCOUNT_CONFLICT",
          details: error.details.length > 0 ? error.details : [],
        });
      }

      if (error.status === 400 || error.status === 422) {
        return res.status(400).json({
          error: message,
          code: error.code,
          details: error.details.length > 0 ? error.details : undefined,
        });
      }

      if (error.status === 401 || error.status === 403) {
        return res.status(503).json({
          error: "Payments provider rejected the request",
          code: error.code || error.type || "FLUTTERWAVE_PROVIDER_REJECTED",
          details: error.details.length > 0 ? error.details : undefined,
        });
      }
    }

    const paymentsConfigErrorCode = resolveFlutterwavePaymentsConfigErrorCode(message);
    if (paymentsConfigErrorCode) {
      return res.status(503).json({
        error: "Payments service is not configured",
        code: paymentsConfigErrorCode,
      });
    }

    console.error("provisionFlutterwaveFundingAccountHandler failed", error);
    return res.status(500).json({ error: "Internal error" });
  }
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

function resolveFundingAccountKyc(rawBody: unknown): { bvn?: string; nin?: string } | undefined {
  const body = rawBody as Record<string, unknown> | undefined;
  if (!body) {
    return undefined;
  }

  const kyc = asRecord(body.kyc);
  const bvn = sanitizeDigits(kyc.bvn ?? body.bvn);
  const nin = sanitizeDigits(kyc.nin ?? body.nin);

  if (!bvn && !nin) {
    return undefined;
  }

  return {
    ...(bvn ? { bvn } : {}),
    ...(nin ? { nin } : {}),
  };
}

function sanitizeDigits(raw: unknown): string | undefined {
  if (typeof raw !== "string" && typeof raw !== "number") {
    return undefined;
  }
  const digits = String(raw).replace(/\D+/g, "");
  return digits || undefined;
}

function asRecord(raw: unknown): Record<string, unknown> {
  return raw && typeof raw === "object" ? (raw as Record<string, unknown>) : {};
}
