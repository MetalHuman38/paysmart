import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { nominatimSearch } from "./nominatim.js";

function okJson(payload: unknown) {
  return {
    ok: true,
    status: 200,
    json: async () => payload,
  };
}

describe("nominatimSearch", () => {
  let fetchMock: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    fetchMock = vi.fn();
    vi.stubGlobal("fetch", fetchMock);
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    vi.clearAllMocks();
  });

  it("returns empty results without calling fetch when query is blank", async () => {
    const result = await nominatimSearch("   ", "gb");

    expect(result).toEqual([]);
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("maps valid rows and applies bounded search when coordinates are provided", async () => {
    fetchMock.mockResolvedValueOnce(
      okJson([
        {
          display_name: "10 Downing Street, Westminster, London, England, United Kingdom",
          lat: "51.5034",
          lon: "-0.1276",
          address: {
            postcode: "SW1A 2AA",
            country_code: "gb",
            house_number: "10",
            road: "Downing Street",
            suburb: "Westminster",
            city: "London",
            state: "England",
          },
        },
        {
          display_name: "invalid-candidate",
          lat: "NaN",
          lon: "-0.1276",
          address: {
            postcode: "SW1A 2AA",
            country_code: "gb",
          },
        },
      ])
    );

    const result = await nominatimSearch(
      "10 Downing Street SW1A 2AA",
      "gb",
      51.5,
      -0.12
    );

    expect(result).toHaveLength(1);
    expect(result[0]).toEqual({
      displayName: "10 Downing Street, Westminster, London, England, United Kingdom",
      lat: 51.5034,
      lon: -0.1276,
      address: {
        postcode: "SW1A 2AA",
        countryCode: "gb",
        line1: "10 Downing Street",
        line2: "Westminster",
        city: "London",
        stateOrRegion: "England",
      },
    });

    const call = fetchMock.mock.calls[0];
    const calledUrl = String(call[0]);
    const requestInit = (call[1] ?? {}) as { headers?: Record<string, string> };

    expect(calledUrl).toContain("countrycodes=gb");
    expect(calledUrl).toContain("bounded=1");
    expect(calledUrl).toContain("viewbox=");
    expect(requestInit.headers).toEqual(
      expect.objectContaining({
        "User-Agent": "PaySmart/1.0",
      })
    );
  });
});
