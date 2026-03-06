import { InvoiceListPage } from "../../domain/model/invoice.js";
import { InvoiceRepository } from "../../domain/repository/InvoiceRepository.js";

export class ListInvoices {
  constructor(private readonly invoices: InvoiceRepository) {}

  async execute(uid: string, limit: number, cursor?: string): Promise<InvoiceListPage> {
    return this.invoices.list(uid, limit, cursor);
  }
}
