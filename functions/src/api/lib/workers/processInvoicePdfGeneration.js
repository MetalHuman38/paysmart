import { FieldValue } from "firebase-admin/firestore";
import { getStorage } from "firebase-admin/storage";
import { initDeps } from "../dependencies.js";
import { authContainer } from "../infrastructure/di/authContainer.js";
import { renderInvoicePdf } from "../services/invoicePdfRenderer.js";
export async function processInvoicePdfGenerationJob(uid, invoiceId) {
    const cleanUid = uid.trim();
    const cleanInvoiceId = invoiceId.trim();
    if (!cleanUid || !cleanInvoiceId) {
        throw new Error("Missing invoice task payload");
    }
    const { firestore, getConfig } = initDeps();
    const config = getConfig();
    const bucketName = config.storageBucket || `${config.projectId}.appspot.com`;
    const invoiceRef = firestore
        .collection("users")
        .doc(cleanUid)
        .collection("invoices")
        .doc(cleanInvoiceId);
    const locked = await firestore.runTransaction(async (tx) => {
        const snap = await tx.get(invoiceRef);
        if (!snap.exists) {
            return null;
        }
        const data = snap.data();
        const pdf = asRecord(data.pdf);
        if (pdf.status === "ready") {
            return {
                fileName: asString(pdf.fileName) || `${asString(data.invoiceNumber) || cleanInvoiceId}.pdf`,
                templateVersion: asString(pdf.templateVersion) || "pay-smart-invoice-v1",
            };
        }
        tx.set(invoiceRef, {
            pdf: {
                status: "processing",
                fileName: asString(pdf.fileName) || `${asString(data.invoiceNumber) || cleanInvoiceId}.pdf`,
                contentType: "application/pdf",
                templateVersion: asString(pdf.templateVersion) || "pay-smart-invoice-v1",
                updatedAt: FieldValue.serverTimestamp(),
                error: null,
            },
            updatedAt: FieldValue.serverTimestamp(),
        }, { merge: true });
        return {
            fileName: asString(pdf.fileName) || `${asString(data.invoiceNumber) || cleanInvoiceId}.pdf`,
            templateVersion: asString(pdf.templateVersion) || "pay-smart-invoice-v1",
        };
    });
    if (!locked) {
        throw new Error("Invoice not found");
    }
    const { invoices } = authContainer();
    try {
        const invoice = await invoices.getById(cleanUid, cleanInvoiceId);
        if (invoice.pdf.status === "ready" && invoice.pdf.objectPath) {
            return;
        }
        const pdfBytes = renderInvoicePdf(invoice);
        const objectPath = `invoices/${cleanUid}/${cleanInvoiceId}/${locked.fileName}`;
        const file = getStorage().bucket(bucketName).file(objectPath);
        await file.save(pdfBytes, {
            resumable: false,
            contentType: "application/pdf",
            metadata: {
                contentDisposition: `attachment; filename="${locked.fileName}"`,
            },
        });
        await invoiceRef.set({
            pdf: {
                status: "ready",
                fileName: locked.fileName,
                contentType: "application/pdf",
                templateVersion: locked.templateVersion,
                objectPath,
                sizeBytes: pdfBytes.byteLength,
                generatedAtMs: Date.now(),
                generatedAt: FieldValue.serverTimestamp(),
                updatedAt: FieldValue.serverTimestamp(),
                error: null,
            },
            updatedAt: FieldValue.serverTimestamp(),
        }, { merge: true });
    }
    catch (error) {
        const message = error instanceof Error ? error.message : "Invoice PDF generation failed";
        await invoiceRef.set({
            pdf: {
                status: "failed",
                error: message.slice(0, 512),
                updatedAt: FieldValue.serverTimestamp(),
            },
            updatedAt: FieldValue.serverTimestamp(),
        }, { merge: true });
        throw error;
    }
}
function asRecord(raw) {
    return raw && typeof raw === "object" ? raw : {};
}
function asString(raw) {
    return typeof raw === "string" ? raw.trim() : "";
}
//# sourceMappingURL=processInvoicePdfGeneration.js.map