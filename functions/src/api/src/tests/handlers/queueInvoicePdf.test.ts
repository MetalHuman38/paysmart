import { beforeEach, describe, expect, it, vi } from "vitest";

const verifyIdToken = vi.fn();
const queuePdf = vi.fn();
const { processInvoicePdfGenerationJob } = vi.hoisted(() => ({
  processInvoicePdfGenerationJob: vi.fn(),
}));

vi.mock("../../dependencies.js", () => ({
  initDeps: () => ({
    auth: {
      verifyIdToken,
    },
    getConfig: () => ({
      projectId: "paysmart-7ee79",
      invoicePdfTaskQueue: "",
      invoicePdfTaskLocation: "europe-west2",
      invoicePdfTaskTargetUrl: "",
      invoicePdfTaskToken: "",
    }),
  }),
}));

vi.mock("../../infrastructure/di/authContainer.js", () => ({
  authContainer: () => ({
    invoices: {
      queuePdf,
    },
  }),
}));

vi.mock("../../workers/processInvoicePdfGeneration.js", () => ({
  processInvoicePdfGenerationJob,
}));

import { queueInvoicePdfHandler } from "../../handlers/queueInvoicePdf.js";

function createResponseRecorder() {
  return {
    statusCode: 200,
    payload: undefined as unknown,
    status(code: number) {
      this.statusCode = code;
      return this;
    },
    json(body: unknown) {
      this.payload = body;
      return this;
    },
  };
}

describe("queueInvoicePdfHandler", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("returns 202 and runs inline fallback when queueing a new pdf job", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    queuePdf.mockResolvedValue({
      invoiceId: "2026-000001",
      status: "queued",
      fileName: "PS-2026-000001.pdf",
      contentType: "application/pdf",
      templateVersion: "pay-smart-invoice-v1",
    });

    const req = {
      headers: { authorization: "Bearer token-1" },
      params: { invoiceId: "2026-000001" },
    };
    const res = createResponseRecorder();

    await queueInvoicePdfHandler(req as any, res as any);

    expect(res.statusCode).toBe(202);
    expect(queuePdf).toHaveBeenCalledWith("uid-1", "2026-000001");
    expect(processInvoicePdfGenerationJob).toHaveBeenCalledWith("uid-1", "2026-000001");
  });

  it("does not enqueue again when pdf is already ready", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    queuePdf.mockResolvedValue({
      invoiceId: "2026-000001",
      status: "ready",
      fileName: "PS-2026-000001.pdf",
      contentType: "application/pdf",
      templateVersion: "pay-smart-invoice-v1",
    });

    const req = {
      headers: { authorization: "Bearer token-1" },
      params: { invoiceId: "2026-000001" },
    };
    const res = createResponseRecorder();

    await queueInvoicePdfHandler(req as any, res as any);

    expect(res.statusCode).toBe(202);
    expect(processInvoicePdfGenerationJob).not.toHaveBeenCalled();
  });
});
