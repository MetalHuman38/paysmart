import { InvoiceDetail } from "../../domain/model/invoice.js";
import { InvoiceRepository } from "../../domain/repository/InvoiceRepository.js";

export class GetInvoiceById {
  constructor(private readonly invoices: InvoiceRepository) {}

  async execute(uid: string, invoiceId: string): Promise<InvoiceDetail> {
    return this.invoices.getById(uid, invoiceId);
  }
}
