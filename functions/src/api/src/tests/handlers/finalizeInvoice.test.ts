import { beforeEach, describe, expect, it, vi } from "vitest";

const verifyIdToken = vi.fn();
const finalize = vi.fn();

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
      finalize,
    },
  }),
}));

import { finalizeInvoiceHandler } from "../../handlers/finalizeInvoice.js";

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

describe("finalizeInvoiceHandler", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("returns 401 when token is missing", async () => {
    const req = { headers: {}, body: {} };
    const res = createResponseRecorder();

    await finalizeInvoiceHandler(req as any, res as any);

    expect(res.statusCode).toBe(401);
    expect(res.payload).toEqual({ error: "Missing token" });
    expect(verifyIdToken).not.toHaveBeenCalled();
  });

  it("returns 200 and finalizes invoice", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    finalize.mockResolvedValue({
      invoiceId: "2026-000001",
      invoiceNumber: "PS-2026-000001",
      status: "finalized",
      sequenceNumber: 1,
      totalHours: 10,
      hourlyRate: 12.5,
      subtotalMinor: 12500,
      currency: "GBP",
      venueName: "Alpha Venue",
      weekEndingDate: "2026-03-08",
      createdAtMs: 1772668800000,
    });

    const req = {
      headers: {
        authorization: "Bearer token-1",
        "idempotency-key": "invoice:week:1",
      },
      body: {
        profile: {
          fullName: "Test User",
          address: "1 Test Street",
          badgeNumber: "B-1",
          badgeExpiryDate: "2027-01-01",
          utrNumber: "123",
          email: "test@example.com",
          paymentMethod: "bank_transfer",
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
          shifts: [{ dayLabel: "Monday", workDate: "2026-03-02", hoursInput: "10" }],
        },
      },
    };
    const res = createResponseRecorder();

    await finalizeInvoiceHandler(req as any, res as any);

    expect(res.statusCode).toBe(200);
    expect(verifyIdToken).toHaveBeenCalledWith("token-1");
    expect(finalize).toHaveBeenCalledWith(
      "uid-1",
      expect.objectContaining({ idempotencyKey: "invoice:week:1" })
    );
  });

  it("returns 400 for validation errors", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    finalize.mockRejectedValue(new Error("weekly.shifts is required"));
    const req = {
      headers: { authorization: "Bearer token-1" },
      body: {},
    };
    const res = createResponseRecorder();

    await finalizeInvoiceHandler(req as any, res as any);

    expect(res.statusCode).toBe(400);
    expect(res.payload).toEqual({ error: "weekly.shifts is required" });
  });
});
