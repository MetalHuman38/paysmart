import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { googleGeocode } from "../../../../../application/usecase/lookupAddress/providers/googleGeocode.js";

const originalGeocodeKey = process.env.GOOGLE_GEOCODE_KEY;

function okJson(payload: unknown) {
  return {
    ok: true,
    status: 200,
    json: async () => payload,
  };
}

describe.sequential("googleGeocode", () => {
  let fetchMock: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    fetchMock = vi.fn();
    vi.stubGlobal("fetch", fetchMock);
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    vi.clearAllMocks();
    process.env.GOOGLE_GEOCODE_KEY = originalGeocodeKey;
  });

  it("returns empty results when API key is missing", async () => {
    delete process.env.GOOGLE_GEOCODE_KEY;

    const result = await googleGeocode("1600 Amphitheatre Pkwy 94043", "us");

    expect(result).toEqual([]);
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("maps valid geocode responses and filters incomplete rows", async () => {
    process.env.GOOGLE_GEOCODE_KEY = "test-geocode-key";
    fetchMock.mockResolvedValueOnce(
      okJson({
        results: [
          {
            formatted_address:
              "1600 Amphitheatre Parkway, Mountain View, CA 94043, USA",
            place_id: "ChIJF4Yf2Ry7j4AR__1AkytDyAE",
            geometry: {
              location: {
                lat: 37.4224764,
                lng: -122.0842499,
              },
            },
            address_components: [
              { long_name: "1600", types: ["street_number"] },
              { long_name: "Amphitheatre Parkway", types: ["route"] },
              { long_name: "94043", types: ["postal_code"] },
              { long_name: "Mountain View", types: ["locality"] },
              { long_name: "California", types: ["administrative_area_level_1"] },
              {
                long_name: "United States",
                short_name: "US",
                types: ["country"],
              },
            ],
          },
          {
            formatted_address: "missing postcode candidate",
            geometry: {
              location: {
                lat: 1,
                lng: 2,
              },
            },
            address_components: [{ long_name: "United States", short_name: "US", types: ["country"] }],
          },
        ],
      })
    );

    const result = await googleGeocode("1600 Amphitheatre Pkwy 94043", "us");

    expect(result).toHaveLength(1);
    expect(result[0]).toEqual({
      displayName: "1600 Amphitheatre Parkway, Mountain View, CA 94043, USA",
      lat: 37.4224764,
      lon: -122.0842499,
      placeId: "ChIJF4Yf2Ry7j4AR__1AkytDyAE",
      address: {
        postcode: "94043",
        countryCode: "us",
        line1: "1600 Amphitheatre Parkway",
        line2: undefined,
        city: "Mountain View",
        stateOrRegion: "California",
      },
    });

    const call = fetchMock.mock.calls[0];
    const calledUrl = String(call[0]);

    expect(calledUrl).toContain("maps.googleapis.com/maps/api/geocode/json");
    expect(calledUrl).toContain("components=country%3Aus");
    expect(calledUrl).toContain("key=test-geocode-key");
  });
});
