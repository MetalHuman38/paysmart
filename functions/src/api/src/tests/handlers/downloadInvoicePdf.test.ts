import { beforeEach, describe, expect, it, vi } from "vitest";

const verifyIdToken = vi.fn();
const downloadPdf = vi.fn();

vi.mock("../../dependencies.js", () => ({
  initDeps: () => ({
    auth: {
      verifyIdToken,
    },
  }),
}));

vi.mock("../../infrastructure/di/authContainer.js", () => ({
  authContainer: () => ({
    invoices: {
      downloadPdf,
    },
  }),
}));

import { downloadInvoicePdfHandler } from "../../handlers/downloadInvoicePdf.js";

function createResponseRecorder() {
  return {
    statusCode: 200,
    payload: undefined as unknown,
    headers: {} as Record<string, string>,
    status(code: number) {
      this.statusCode = code;
      return this;
    },
    json(body: unknown) {
      this.payload = body;
      return this;
    },
    send(body: unknown) {
      this.payload = body;
      return this;
    },
    setHeader(name: string, value: string) {
      this.headers[name] = value;
    },
  };
}

describe("downloadInvoicePdfHandler", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("streams the pdf when ready", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    downloadPdf.mockResolvedValue({
      fileName: "PS-2026-000001.pdf",
      contentType: "application/pdf",
      bytes: Buffer.from("%PDF-1.4"),
    });
    const req = {
      headers: { authorization: "Bearer token-1" },
      params: { invoiceId: "2026-000001" },
    };
    const res = createResponseRecorder();

    await downloadInvoicePdfHandler(req as any, res as any);

    expect(res.statusCode).toBe(200);
    expect(res.headers["Content-Type"]).toBe("application/pdf");
    expect(String(res.headers["Content-Disposition"])).toContain("PS-2026-000001.pdf");
    expect(downloadPdf).toHaveBeenCalledWith("uid-1", "2026-000001");
  });

  it("returns 409 when pdf is not ready", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    downloadPdf.mockRejectedValue(new Error("Invoice PDF is not ready"));
    const req = {
      headers: { authorization: "Bearer token-1" },
      params: { invoiceId: "2026-000001" },
    };
    const res = createResponseRecorder();

    await downloadInvoicePdfHandler(req as any, res as any);

    expect(res.statusCode).toBe(409);
    expect(res.payload).toEqual({ error: "Invoice PDF is not ready" });
  });
});
