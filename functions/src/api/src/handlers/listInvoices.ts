import { Request, Response } from "express";
import { ListInvoices } from "../application/usecase/ListInvoices.js";
import { initDeps } from "../dependencies.js";
import { authContainer } from "../infrastructure/di/authContainer.js";

const DEFAULT_LIMIT = 20;
const MAX_LIMIT = 100;

export async function listInvoicesHandler(req: Request, res: Response) {
  try {
    const authHeader = req.headers.authorization;
    if (!authHeader?.startsWith("Bearer ")) {
      return res.status(401).json({ error: "Missing token" });
    }

    const limit = parseLimit(req.query.limit);
    const cursor = parseCursor(req.query.cursor);
    const idToken = authHeader.split("Bearer ")[1];
    const { auth } = initDeps();
    const decoded = await auth.verifyIdToken(idToken);

    const { invoices } = authContainer();
    const useCase = new ListInvoices(invoices);
    const result = await useCase.execute(decoded.uid, limit, cursor);

    return res.status(200).json(result);
  } catch (error) {
    const message = error instanceof Error ? error.message : "Internal error";
    if (message.includes("limit")) {
      return res.status(400).json({ error: message });
    }
    if (message.includes("cursor")) {
      return res.status(400).json({ error: message });
    }

    console.error("listInvoicesHandler failed", error);
    return res.status(500).json({ error: "Internal error" });
  }
}

function parseLimit(raw: unknown): number {
  if (raw == null || raw === "") {
    return DEFAULT_LIMIT;
  }

  const parsed = Number(raw);
  if (!Number.isFinite(parsed) || parsed <= 0) {
    throw new Error("limit must be a positive number");
  }

  return Math.min(Math.floor(parsed), MAX_LIMIT);
}

function parseCursor(raw: unknown): string | undefined {
  if (raw == null || raw === "") {
    return undefined;
  }
  if (typeof raw !== "string") {
    throw new Error("cursor must be a string");
  }
  const clean = raw.trim();
  if (!clean) {
    return undefined;
  }
  if (clean.length > 128) {
    throw new Error("cursor is too long");
  }
  return clean;
}
