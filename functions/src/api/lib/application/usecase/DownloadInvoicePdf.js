export class DownloadInvoicePdf {
    invoices;
    constructor(invoices) {
        this.invoices = invoices;
    }
    async execute(uid, invoiceId) {
        return this.invoices.downloadPdf(uid, invoiceId);
    }
}
//# sourceMappingURL=DownloadInvoicePdf.js.map