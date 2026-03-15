import { type Express } from "express";
import { finalizeInvoiceHandler } from "../handlers/finalizeInvoice.js";
import { downloadInvoicePdfHandler } from "../handlers/downloadInvoicePdf.js";
import { getInvoiceByIdHandler } from "../handlers/getInvoiceById.js";
import { listInvoicesHandler } from "../handlers/listInvoices.js";
import { processInvoicePdfTaskHandler } from "../handlers/processInvoicePdfTask.js";
import { queueInvoicePdfHandler } from "../handlers/queueInvoicePdf.js";
import { requireActiveSession } from "../middleware/requireActiveSession.js";
import { corsify } from "../utils.js";

export function mountInvoiceRoutes(app: Express) {
  app.post("/internal/tasks/invoices/pdf", processInvoicePdfTaskHandler);
 
  app.post("/invoices/finalize", requireActiveSession, finalizeInvoiceHandler);
  app.options("/invoices/finalize", (_, res) => {
    corsify(res);
    res.status(204).end();                       
  });

  app.get("/invoices", requireActiveSession, listInvoicesHandler);
  app.options("/invoices", (_, res) => {
    corsify(res);
    res.status(204).end();
  });

  app.get("/invoices/:invoiceId", requireActiveSession, getInvoiceByIdHandler);
  app.options("/invoices/:invoiceId", (_, res) => {
    corsify(res);
    res.status(204).end();
  });

  app.post("/invoices/:invoiceId/pdf", requireActiveSession, queueInvoicePdfHandler);
  app.options("/invoices/:invoiceId/pdf", (_, res) => {
    corsify(res);
    res.status(204).end();
  });

  app.get("/invoices/:invoiceId/pdf/download", requireActiveSession, downloadInvoicePdfHandler);
  app.options("/invoices/:invoiceId/pdf/download", (_, res) => {
    corsify(res);
    res.status(204).end();
  });
}
