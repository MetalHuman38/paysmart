import { beforeEach, describe, expect, it, vi } from "vitest";

const verifyIdToken = vi.fn();
const getById = vi.fn();

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
      getById,
    },
  }),
}));

import { getInvoiceByIdHandler } from "../../handlers/getInvoiceById.js";

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

describe("getInvoiceByIdHandler", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("returns 401 when token is missing", async () => {
    const req = { headers: {}, params: { invoiceId: "2026-000001" } };
    const res = createResponseRecorder();

    await getInvoiceByIdHandler(req as any, res as any);

    expect(res.statusCode).toBe(401);
    expect(res.payload).toEqual({ error: "Missing token" });
    expect(verifyIdToken).not.toHaveBeenCalled();
  });

  it("returns 200 with invoice detail", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    getById.mockResolvedValue({
      invoiceId: "2026-000001",
      invoiceNumber: "PS-2026-000001",
      status: "finalized",
      subtotalMinor: 25000,
      currency: "GBP",
      venueName: "Alpha Venue",
      weekEndingDate: "2026-03-08",
      createdAtMs: 1772668800000,
      sequenceNumber: 1,
      totalHours: 20,
      hourlyRate: 12.5,
      profile: {
        fullName: "Test User",
        address: "1 Test Street",
        badgeNumber: "B-1",
        badgeExpiryDate: "2027-01-01",
        utrNumber: "123",
        email: "test@example.com",
        contactPhone: "",
        paymentMethod: "bank_transfer",
        accountNumber: "",
        sortCode: "",
        paymentInstructions: "",
        declaration: "decl",
      },
      venue: {
        venueId: "venue_1",
        venueName: "Alpha Venue",
        venueAddress: "Address",
      },
      weekly: {
        invoiceDate: "2026-03-03",
        weekEndingDate: "2026-03-08",
        hourlyRateInput: "12.5",
        shifts: [{ dayLabel: "Monday", workDate: "2026-03-02", hoursInput: "20" }],
      },
    });

    const req = {
      headers: {
        authorization: "Bearer token-1",
      },
      params: {
        invoiceId: "2026-000001",
      },
    };
    const res = createResponseRecorder();

    await getInvoiceByIdHandler(req as any, res as any);

    expect(res.statusCode).toBe(200);
    expect(verifyIdToken).toHaveBeenCalledWith("token-1");
    expect(getById).toHaveBeenCalledWith("uid-1", "2026-000001");
  });

  it("returns 404 when invoice does not exist", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    getById.mockRejectedValue(new Error("Invoice not found"));
    const req = {
      headers: { authorization: "Bearer token-1" },
      params: { invoiceId: "missing" },
    };
    const res = createResponseRecorder();

    await getInvoiceByIdHandler(req as any, res as any);

    expect(res.statusCode).toBe(404);
    expect(res.payload).toEqual({ error: "Invoice not found" });
  });
});
