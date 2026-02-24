import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { googleAddressValidationLookup } from "./googleAddressValidation.js";
const originalAddressValidationKey = process.env.GOOGLE_ADDRESS_VALIDATION_KEY;
function okJson(payload) {
    return {
        ok: true,
        status: 200,
        json: async () => payload,
    };
}
describe.sequential("googleAddressValidationLookup", () => {
    let fetchMock;
    beforeEach(() => {
        fetchMock = vi.fn();
        vi.stubGlobal("fetch", fetchMock);
    });
    afterEach(() => {
        vi.unstubAllGlobals();
        vi.clearAllMocks();
        process.env.GOOGLE_ADDRESS_VALIDATION_KEY = originalAddressValidationKey;
    });
    it("returns null when API key is missing", async () => {
        delete process.env.GOOGLE_ADDRESS_VALIDATION_KEY;
        const result = await googleAddressValidationLookup({
            house: "1600 Amphitheatre Pkwy",
            postcode: "94043",
            country: "us",
        });
        expect(result).toBeNull();
        expect(fetchMock).not.toHaveBeenCalled();
    });
    it("maps a complete premise-level validation to VERIFIED", async () => {
        process.env.GOOGLE_ADDRESS_VALIDATION_KEY = "test-address-validation-key";
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
                },
            },
            responseId: "provider-response-1",
        }));
        const result = await googleAddressValidationLookup({
            house: "1600 Amphitheatre Pkwy",
            postcode: "94043",
            country: "us",
        });
        expect(result).not.toBeNull();
        expect(result).toEqual(expect.objectContaining({
            status: "OK",
            decision: "VERIFIED",
            decisionReasons: [],
            source: "google_address_validation",
            postCode: "94043-1351",
            countryCode: "US",
            providerResponseId: "provider-response-1",
        }));
        expect(result?.mapPin).toEqual(expect.objectContaining({
            lat: 37.4225022,
            lng: -122.0847398,
            placeId: "ChIJF4Yf2Ry7j4AR__1AkytDyAE",
        }));
        expect(fetchMock).toHaveBeenCalledTimes(1);
        const call = fetchMock.mock.calls[0];
        const calledUrl = String(call[0]);
        const requestInit = (call[1] ?? {});
        const requestBody = JSON.parse(String(requestInit.body));
        expect(calledUrl).toContain("addressvalidation.googleapis.com/v1:validateAddress");
        expect(calledUrl).toContain("key=test-address-validation-key");
        expect(requestInit.method).toBe("POST");
        expect(requestBody).toEqual(expect.objectContaining({
            address: expect.objectContaining({
                regionCode: "US",
                postalCode: "94043",
                addressLines: ["1600 Amphitheatre Pkwy 94043"],
            }),
            enableUspsCass: true,
        }));
    });
    it("returns REJECT when postcode mismatches the validated response", async () => {
        process.env.GOOGLE_ADDRESS_VALIDATION_KEY = "test-address-validation-key";
        fetchMock.mockResolvedValueOnce(okJson({
            result: {
                verdict: {
                    addressComplete: true,
                    validationGranularity: "PREMISE",
                    geocodeGranularity: "PREMISE",
                },
                address: {
                    formattedAddress: "1600 Amphitheatre Parkway, Mountain View, CA, USA",
                    postalAddress: {
                        regionCode: "US",
                        postalCode: "99999",
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
        const result = await googleAddressValidationLookup({
            house: "1600 Amphitheatre Pkwy",
            postcode: "94043",
            country: "us",
        });
        expect(result?.decision).toBe("REJECT");
        expect(result?.decisionReasons).toContain("postcode_mismatch");
    });
});
//# sourceMappingURL=googleAddressValidation.test.js.map