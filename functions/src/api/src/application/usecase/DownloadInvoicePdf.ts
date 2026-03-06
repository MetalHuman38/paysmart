import { InvoicePdfDownload } from "../../domain/model/invoice.js";
import { InvoiceRepository } from "../../domain/repository/InvoiceRepository.js";

export class DownloadInvoicePdf {
  constructor(private readonly invoices: InvoiceRepository) {}

  async execute(uid: string, invoiceId: string): Promise<InvoicePdfDownload> {
    return this.invoices.downloadPdf(uid, invoiceId);
  }
}
