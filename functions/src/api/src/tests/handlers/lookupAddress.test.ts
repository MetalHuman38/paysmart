import { beforeEach, describe, expect, it, vi } from "vitest";

const { execute } = vi.hoisted(() => ({
  execute: vi.fn(),
}));

vi.mock("../../application/usecase/LookupAddress.js", () => ({
  LookupAddress: class {
    execute = execute;
  },
}));

import { lookupAddressHandler } from "../../handlers/lookupAddress.js";

type TestReq = {
  body?: Record<string, unknown>;
};

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

describe("lookupAddressHandler", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("returns 400 when postcode is missing", async () => {
    const req = {
      body: { house: "10 Downing Street" },
    } as TestReq;
    const res = createResponseRecorder();

    await lookupAddressHandler(req as any, res as any);

    expect(res.statusCode).toBe(400);
    expect(res.payload).toEqual({ error: "postcode is required" });
  });

  it("returns resolved payload when address is found", async () => {
    execute.mockResolvedValue({
      status: "OK",
      decision: "VERIFIED",
      decisionReasons: [],
      fullAddress: "10 Downing Street, Westminster, London, England, United Kingdom",
      lat: 51.5034,
      lng: -0.1276,
      postCode: "SW1A2AA",
      houseInfo: "10 Downing Street",
      countryCode: "GB",
      fullAddressWithHouse: "10 Downing Street, Westminster, London, England, United Kingdom",
      line1: "10 Downing Street",
      city: "London",
      source: "google_address_validation",
      mapPin: {
        lat: 51.5034,
        lng: -0.1276,
        label: "10 Downing Street, Westminster, London, England, United Kingdom",
      },
    });

    const req = {
      body: {
        house: "10 Downing Street",
        postcode: "SW1A 2AA",
        country: "gb",
      },
    } as TestReq;
    const res = createResponseRecorder();

    await lookupAddressHandler(req as any, res as any);

    expect(res.statusCode).toBe(200);
    expect(execute).toHaveBeenCalledWith({
      house: "10 Downing Street",
      postcode: "SW1A 2AA",
      country: "gb",
      lat: undefined,
      lng: undefined,
    });
    expect(res.payload).toEqual(
      expect.objectContaining({
        status: "OK",
        decision: "VERIFIED",
        postCode: "SW1A2AA",
        countryCode: "GB",
      })
    );
  });
});
