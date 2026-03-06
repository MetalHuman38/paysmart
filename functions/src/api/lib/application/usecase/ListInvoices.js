export class ListInvoices {
    invoices;
    constructor(invoices) {
        this.invoices = invoices;
    }
    async execute(uid, limit, cursor) {
        return this.invoices.list(uid, limit, cursor);
    }
}
//# sourceMappingURL=ListInvoices.js.map