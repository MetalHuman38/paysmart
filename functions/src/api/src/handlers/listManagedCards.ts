import { Request, Response } from "express";
import { initDeps } from "../dependencies.js";
import { authContainer } from "../infrastructure/di/authContainer.js";
import { ListManagedCards } from "../application/usecase/ListManagedCards.js";

export async function listManagedCardsHandler(req: Request, res: Response) {
  try {
    const decoded = await verifyBearerToken(req);
    const { managedCards } = authContainer();
    const useCase = new ListManagedCards(managedCards);
    const result = await useCase.execute(decoded.uid);
    return res.status(200).json(result);
  } catch (error) {
    return handleManagedCardError(error, res, "listManagedCardsHandler failed");
  }
}

async function verifyBearerToken(req: Request) {
  const authHeader = req.headers.authorization;
  if (!authHeader?.startsWith("Bearer ")) {
    throw new Error("Missing token");
  }

  const idToken = authHeader.split("Bearer ")[1];
  const { auth } = initDeps();
  return auth.verifyIdToken(idToken);
}

export function handleManagedCardError(
  error: unknown,
  res: Response,
  logLabel: string
) {
  const message = error instanceof Error ? error.message : "Internal error";
  if (message === "Missing token") {
    return res.status(401).json({ error: "Missing token" });
  }
  if (
    message.includes("Invalid") ||
    message.includes("Missing") ||
    message.includes("No such") ||
    message.includes("not found") ||
    message.includes("already detached") ||
    message.includes("belongs to another customer")
  ) {
    return res.status(400).json({ error: message });
  }

  console.error(logLabel, error);
  return res.status(500).json({ error: "Internal error" });
}
