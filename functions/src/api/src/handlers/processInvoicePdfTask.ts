import { Request, Response } from "express";
import { initDeps } from "../dependencies.js";
import { processInvoicePdfGenerationJob } from "../workers/processInvoicePdfGeneration.js";

export async function processInvoicePdfTaskHandler(req: Request, res: Response) {
  try {
    const config = initDeps().getConfig();
    const expectedToken = config.invoicePdfTaskToken.trim();
    const providedToken = String(req.header("X-Invoice-Task-Token") || "").trim();

    if (!expectedToken || providedToken !== expectedToken) {
      return res.status(401).json({ error: "Unauthorized" });
    }

    const uid = typeof req.body?.uid === "string" ? req.body.uid.trim() : "";
    const invoiceId =
      typeof req.body?.invoiceId === "string" ? req.body.invoiceId.trim() : "";

    if (!uid || !invoiceId) {
      return res.status(400).json({ error: "Missing invoice task payload" });
    }

    await processInvoicePdfGenerationJob(uid, invoiceId);
    return res.status(200).json({ ok: true });
  } catch (error) {
    console.error("processInvoicePdfTaskHandler failed", error);
    return res.status(500).json({ error: "Internal error" });
  }
}
