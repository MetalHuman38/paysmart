import { QueueInvoicePdfResult } from "../../domain/model/invoice.js";
import { InvoiceRepository } from "../../domain/repository/InvoiceRepository.js";

export class QueueInvoicePdf {
  constructor(private readonly invoices: InvoiceRepository) {}

  async execute(uid: string, invoiceId: string): Promise<QueueInvoicePdfResult> {
    return this.invoices.queuePdf(uid, invoiceId);
  }
}
