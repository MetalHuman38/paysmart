import { Request, Response } from "express";
import { initDeps } from "../dependencies.js";
import { GetFlutterwaveFundingAccount } from "../application/usecase/GetFlutterwaveFundingAccount.js";
import { authContainer } from "../infrastructure/di/authContainer.js";
import { resolveFlutterwavePaymentsConfigErrorCode } from "./utils/flutterwavePaymentsConfigError.js";

export async function getFlutterwaveFundingAccountHandler(
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
    const useCase = new GetFlutterwaveFundingAccount(flutterwaveFundingAccounts);
    const fundingAccount = await useCase.execute(decoded.uid);

    if (!fundingAccount) {
      return res.status(404).json({
        error: "Funding account not found",
        code: "FLUTTERWAVE_FUNDING_ACCOUNT_NOT_FOUND",
      });
    }

    return res.status(200).json(fundingAccount);
  } catch (error) {
    const message = error instanceof Error ? error.message : "Internal error";
    const paymentsConfigErrorCode = resolveFlutterwavePaymentsConfigErrorCode(message);

    if (paymentsConfigErrorCode) {
      return res.status(503).json({
        error: "Payments service is not configured",
        code: paymentsConfigErrorCode,
      });
    }

    console.error("getFlutterwaveFundingAccountHandler failed", error);
    return res.status(500).json({ error: "Internal error" });
  }
}
