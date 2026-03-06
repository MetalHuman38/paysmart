import { QueueInvoicePdf } from "../application/usecase/QueueInvoicePdf.js";
import { initDeps } from "../dependencies.js";
import { authContainer } from "../infrastructure/di/authContainer.js";
import { GoogleCloudAccessTokenProvider } from "../services/googleCloudAccessTokenProvider.js";
import { InvoicePdfTaskService } from "../services/invoicePdfTaskService.js";
import { processInvoicePdfGenerationJob } from "../workers/processInvoicePdfGeneration.js";
export async function queueInvoicePdfHandler(req, res) {
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
        const { auth, getConfig } = initDeps();
        const decoded = await auth.verifyIdToken(idToken);
        const { invoices } = authContainer();
        const useCase = new QueueInvoicePdf(invoices);
        const result = await useCase.execute(decoded.uid, invoiceId);
        const config = getConfig();
        const taskService = new InvoicePdfTaskService(new GoogleCloudAccessTokenProvider(), {
            projectId: config.projectId,
            queue: config.invoicePdfTaskQueue,
            location: config.invoicePdfTaskLocation,
            targetUrl: config.invoicePdfTaskTargetUrl,
            token: config.invoicePdfTaskToken,
        });
        if (result.status === "queued") {
            if (taskService.isConfigured()) {
                await taskService.enqueue({ uid: decoded.uid, invoiceId });
            }
            else {
                await processInvoicePdfGenerationJob(decoded.uid, invoiceId);
            }
        }
        return res.status(202).json(result);
    }
    catch (error) {
        const message = error instanceof Error ? error.message : "Internal error";
        if (message.includes("Invoice not found")) {
            return res.status(404).json({ error: "Invoice not found" });
        }
        if (message.includes("Missing invoiceId")) {
            return res.status(400).json({ error: "Missing invoiceId" });
        }
        console.error("queueInvoicePdfHandler failed", error);
        return res.status(500).json({ error: "Internal error" });
    }
}
//# sourceMappingURL=queueInvoicePdf.js.map