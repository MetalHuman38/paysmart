import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { LookupAddress } from "./LookupAddress.js";
const originalAddressValidationKey = process.env.GOOGLE_ADDRESS_VALIDATION_KEY;
const originalGeocodeKey = process.env.GOOGLE_GEOCODE_KEY;
function okJson(payload) {
    return {
        ok: true,
        status: 200,
        json: async () => payload,
    };
}
describe.sequential("LookupAddress", () => {
    let fetchMock;
    beforeEach(() => {
        fetchMock = vi.fn();
        vi.stubGlobal("fetch", fetchMock);
        process.env.GOOGLE_ADDRESS_VALIDATION_KEY = "test-address-validation-key";
        delete process.env.GOOGLE_GEOCODE_KEY;
    });
    afterEach(() => {
        vi.unstubAllGlobals();
        vi.clearAllMocks();
        process.env.GOOGLE_ADDRESS_VALIDATION_KEY = originalAddressValidationKey;
        process.env.GOOGLE_GEOCODE_KEY = originalGeocodeKey;
    });
    it("returns VERIFIED when Google Address Validation is precise and complete", async () => {
        fetchMock.mockResolvedValueOnce(okJson({
            result: {
                verdict: {
                    addressComplete: true,
                    validationGranularity: "PREMISE",
                    geocodeGranularity: "PREMISE",
                },
                address: {
                    formattedAddress: "1600 Amphitheatre Parkway, Mountain View, CA 94043-1351, USA",
                    postalAddress: {
                        regionCode: "US",
                        postalCode: "94043-1351",
                        administrativeArea: "CA",
                        locality: "Mountain View",
                        addressLines: ["1600 Amphitheatre Pkwy"],
                    },
                },
                geocode: {
                    location: {
                        latitude: 37.4225022,
                        longitude: -122.0847398,
                    },
                    placeId: "ChIJF4Yf2Ry7j4AR__1AkytDyAE",
                    plusCode: {
                        globalCode: "849VCWF8+24",
                    },
                },
            },
            responseId: "response-1",
        }));
        const useCase = new LookupAddress();
        const result = await useCase.execute({
            house: "1600 Amphitheatre Pkwy",
            postcode: "94043",
            country: "us",
        });
        expect(result).not.toBeNull();
        expect(result).toEqual(expect.objectContaining({
            status: "OK",
            decision: "VERIFIED",
            source: "google_address_validation",
            postCode: "94043-1351",
            countryCode: "US",
            providerResponseId: "response-1",
        }));
        expect(result?.decisionReasons).toEqual([]);
        expect(result?.mapPin).toEqual(expect.objectContaining({
            lat: 37.4225022,
            lng: -122.0847398,
            placeId: "ChIJF4Yf2Ry7j4AR__1AkytDyAE",
        }));
    });
    it("returns REVIEW when Google verdict indicates inferred components", async () => {
        fetchMock.mockResolvedValueOnce(okJson({
            result: {
                verdict: {
                    addressComplete: true,
                    validationGranularity: "PREMISE",
                    geocodeGranularity: "PREMISE",
                    hasInferredComponents: true,
                },
                address: {
                    formattedAddress: "1600 Amphitheatre Parkway, Mountain View, CA 94043-1351, USA",
                    postalAddress: {
                        regionCode: "US",
                        postalCode: "94043-1351",
                        administrativeArea: "CA",
                        locality: "Mountain View",
                        addressLines: ["1600 Amphitheatre Pkwy"],
                    },
                },
                geocode: {
                    location: {
                        latitude: 37.4225022,
                        longitude: -122.0847398,
                    },
                },
            },
        }));
        const useCase = new LookupAddress();
        const result = await useCase.execute({
            house: "1600 Amphitheatre Pkwy",
            postcode: "94043",
            country: "us",
        });
        expect(result?.decision).toBe("REVIEW");
        expect(result?.decisionReasons).toContain("inferred_components");
    });
    it("returns REJECT when postcode mismatches validated postcode", async () => {
        fetchMock.mockResolvedValueOnce(okJson({
            result: {
                verdict: {
                    addressComplete: true,
                    validationGranularity: "PREMISE",
                    geocodeGranularity: "PREMISE",
                },
                address: {
                    formattedAddress: "Invalid match example",
                    postalAddress: {
                        regionCode: "US",
                        postalCode: "99999",
                        administrativeArea: "CA",
                        locality: "Mountain View",
                        addressLines: ["1600 Amphitheatre Pkwy"],
                    },
                },
                geocode: {
                    location: {
                        latitude: 37.4225022,
                        longitude: -122.0847398,
                    },
                },
            },
        }));
        const useCase = new LookupAddress();
        const result = await useCase.execute({
            house: "1600 Amphitheatre Pkwy",
            postcode: "94043",
            country: "us",
        });
        expect(result?.decision).toBe("REJECT");
        expect(result?.decisionReasons).toContain("postcode_mismatch");
    });
    it("falls back to nominatim when Google Address Validation is unavailable", async () => {
        fetchMock.mockRejectedValueOnce(new Error("Address validation timeout"));
        fetchMock.mockResolvedValueOnce(okJson([
            {
                display_name: "1600 Amphitheatre Pkwy, Mountain View, California, United States",
                lat: "37.4225022",
                lon: "-122.0847398",
                address: {
                    postcode: "94043",
                    country_code: "us",
                    house_number: "1600",
                    road: "Amphitheatre Pkwy",
                    city: "Mountain View",
                    state: "California",
                },
            },
        ]));
        const useCase = new LookupAddress();
        const result = await useCase.execute({
            house: "1600 Amphitheatre Pkwy",
            postcode: "94043",
            country: "us",
        });
        expect(fetchMock).toHaveBeenCalledTimes(2);
        expect(result?.source).toBe("nominatim");
        expect(result?.decision).toBe("REVIEW");
        expect(result?.decisionReasons).toContain("fallback_geocode_only");
    });
});
//# sourceMappingURL=LookupAddress.test.js.map