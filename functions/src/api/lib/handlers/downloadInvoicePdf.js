import { DownloadInvoicePdf } from "../application/usecase/DownloadInvoicePdf.js";
import { initDeps } from "../dependencies.js";
import { authContainer } from "../infrastructure/di/authContainer.js";
export async function downloadInvoicePdfHandler(req, res) {
    try {
        const authHeader = req.headers.authorization;
        if (!authHeader?.startsWith("Bearer ")) {
            return res.status(401).json({ error: "Missing token" });
        }
        const invoiceId = req.params.invoiceId?.trim();
        if (!invoiceId) {
            return res.status(400).json({ error: "Missing invoiceId" });
        }
        const idToken = authHeader.split("Bearer ")[1];
        const { auth } = initDeps();
        const decoded = await auth.verifyIdToken(idToken);
        const { invoices } = authContainer();
        const useCase = new DownloadInvoicePdf(invoices);
        const result = await useCase.execute(decoded.uid, invoiceId);
        res.setHeader("Content-Type", result.contentType);
        res.setHeader("Content-Disposition", `attachment; filename=\"${result.fileName}\"`);
        return res.status(200).send(result.bytes);
    }
    catch (error) {
        const message = error instanceof Error ? error.message : "Internal error";
        if (message.includes("Invoice not found")) {
            return res.status(404).json({ error: "Invoice not found" });
        }
        if (message.includes("not ready")) {
            return res.status(409).json({ error: "Invoice PDF is not ready" });
        }
        if (message.includes("file is missing")) {
            return res.status(404).json({ error: "Invoice PDF file is missing" });
        }
        if (message.includes("Missing invoiceId")) {
            return res.status(400).json({ error: "Missing invoiceId" });
        }
        console.error("downloadInvoicePdfHandler failed", error);
        return res.status(500).json({ error: "Internal error" });
    }
}
//# sourceMappingURL=downloadInvoicePdf.js.map