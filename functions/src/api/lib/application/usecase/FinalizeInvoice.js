export class FinalizeInvoice {
    invoices;
    constructor(invoices) {
        this.invoices = invoices;
    }
    async execute(uid, input) {
        return this.invoices.finalize(uid, input);
    }
}
//# sourceMappingURL=FinalizeInvoice.js.map