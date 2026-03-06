import { FinalizeInvoiceInput, FinalizedInvoice } from "../../domain/model/invoice.js";
import { InvoiceRepository } from "../../domain/repository/InvoiceRepository.js";

export class FinalizeInvoice {
  constructor(private readonly invoices: InvoiceRepository) {}

  async execute(uid: string, input: FinalizeInvoiceInput): Promise<FinalizedInvoice> {
    return this.invoices.finalize(uid, input);
  }
}
