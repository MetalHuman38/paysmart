export class GetInvoiceById {
    invoices;
    constructor(invoices) {
        this.invoices = invoices;
    }
    async execute(uid, invoiceId) {
        return this.invoices.getById(uid, invoiceId);
    }
}
//# sourceMappingURL=GetInvoiceById.js.map