import { beforeEach, describe, expect, it, vi } from "vitest";

const verifyIdToken = vi.fn();
const list = vi.fn();

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
      list,
    },
  }),
}));

import { listInvoicesHandler } from "../../handlers/listInvoices.js";

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

describe("listInvoicesHandler", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("returns 401 when token is missing", async () => {
    const req = { headers: {}, query: {} };
    const res = createResponseRecorder();

    await listInvoicesHandler(req as any, res as any);

    expect(res.statusCode).toBe(401);
    expect(res.payload).toEqual({ error: "Missing token" });
    expect(verifyIdToken).not.toHaveBeenCalled();
  });

  it("returns 200 with invoice items", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    list.mockResolvedValue({
      items: [
        {
          invoiceId: "2026-000001",
          invoiceNumber: "PS-2026-000001",
          status: "finalized",
          subtotalMinor: 10000,
          currency: "GBP",
          venueName: "Alpha Venue",
          weekEndingDate: "2026-03-08",
          createdAtMs: 1772668800000,
        },
      ],
      nextCursor: "2026-000001",
    });
    const req = {
      headers: {
        authorization: "Bearer token-1",
      },
      query: {
        limit: "10",
        cursor: "2026-000000",
      },
    };
    const res = createResponseRecorder();

    await listInvoicesHandler(req as any, res as any);

    expect(res.statusCode).toBe(200);
    expect(verifyIdToken).toHaveBeenCalledWith("token-1");
    expect(list).toHaveBeenCalledWith("uid-1", 10, "2026-000000");
    expect(res.payload).toEqual({ items: expect.any(Array), nextCursor: "2026-000001" });
  });

  it("returns 400 for invalid limit", async () => {
    const req = {
      headers: { authorization: "Bearer token-1" },
      query: { limit: "abc" },
    };
    const res = createResponseRecorder();

    await listInvoicesHandler(req as any, res as any);

    expect(res.statusCode).toBe(400);
    expect(res.payload).toEqual({ error: "limit must be a positive number" });
  });

  it("returns 400 for non-string cursor", async () => {
    const req = {
      headers: { authorization: "Bearer token-1" },
      query: { cursor: 1234 },
    };
    const res = createResponseRecorder();

    await listInvoicesHandler(req as any, res as any);

    expect(res.statusCode).toBe(400);
    expect(res.payload).toEqual({ error: "cursor must be a string" });
  });
});
