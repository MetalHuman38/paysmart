import {
  FinalizeInvoiceInput,
  FinalizedInvoice,
  InvoiceDetail,
  InvoicePdfDownload,
  InvoiceListPage,
  QueueInvoicePdfResult,
} from "../model/invoice.js";

export interface InvoiceRepository {
  finalize(uid: string, input: FinalizeInvoiceInput): Promise<FinalizedInvoice>;
  getById(uid: string, invoiceId: string): Promise<InvoiceDetail>;
  list(uid: string, limit: number, cursor?: string): Promise<InvoiceListPage>;
  queuePdf(uid: string, invoiceId: string): Promise<QueueInvoicePdfResult>;
  downloadPdf(uid: string, invoiceId: string): Promise<InvoicePdfDownload>;
}
