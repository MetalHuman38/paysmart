export class QueueInvoicePdf {
    invoices;
    constructor(invoices) {
        this.invoices = invoices;
    }
    async execute(uid, invoiceId) {
        return this.invoices.queuePdf(uid, invoiceId);
    }
}
//# sourceMappingURL=QueueInvoicePdf.js.map