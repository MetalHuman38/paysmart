import { Request, Response } from "express";
import { initDeps } from "../dependencies.js";
import { authContainer } from "../infrastructure/di/authContainer.js";
import { GetFlutterwaveAddMoneySessionStatus } from "../application/usecase/GetFlutterwaveAddMoneySessionStatus.js";

export async function getFlutterwaveAddMoneySessionStatusHandler(
  req: Request,
  res: Response
) {
  try {
    const authHeader = req.headers.authorization;
    if (!authHeader?.startsWith("Bearer ")) {
      return res.status(401).json({ error: "Missing token" });
    }

    const sessionId = req.params.sessionId?.toString().trim();
    if (!sessionId) {
      return res.status(400).json({ error: "Missing sessionId" });
    }

    const idToken = authHeader.split("Bearer ")[1];
    const { auth } = initDeps();
    const decoded = await auth.verifyIdToken(idToken);

    const { addMoneyFlutterwave } = authContainer();
    const useCase = new GetFlutterwaveAddMoneySessionStatus(addMoneyFlutterwave);
    const session = await useCase.execute(decoded.uid, sessionId);

    return res.status(200).json(session);
  } catch (error) {
    const message = error instanceof Error ? error.message : "Internal error";

    if (message.includes("not found")) {
      return res.status(404).json({ error: "Session not found" });
    }

    if (message.includes("Missing")) {
      return res.status(400).json({ error: message });
    }

    console.error("getFlutterwaveAddMoneySessionStatusHandler failed", error);
    return res.status(500).json({ error: "Internal error" });
  }
}
