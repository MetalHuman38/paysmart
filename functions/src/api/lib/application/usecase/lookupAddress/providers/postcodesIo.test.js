import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { postcodesIoLookup } from "./postcodesIo.js";
function okJson(payload) {
    return {
        ok: true,
        status: 200,
        json: async () => payload,
    };
}
describe("postcodesIoLookup", () => {
    let fetchMock;
    beforeEach(() => {
        fetchMock = vi.fn();
        vi.stubGlobal("fetch", fetchMock);
    });
    afterEach(() => {
        vi.unstubAllGlobals();
        vi.clearAllMocks();
    });
    it("returns null when postcodes.io does not return an OK status payload", async () => {
        fetchMock.mockResolvedValueOnce(okJson({
            status: 404,
        }));
        const result = await postcodesIoLookup("SW1A 2AA", "10 Downing Street");
        expect(result).toBeNull();
    });
    it("maps a successful postcodes.io payload to an address candidate", async () => {
        fetchMock.mockResolvedValueOnce(okJson({
            status: 200,
            result: {
                admin_ward: "St James's",
                admin_district: "Westminster",
                region: "London",
                country: "England",
                postcode: "SW1A 2AA",
                latitude: 51.5034,
                longitude: -0.1276,
            },
        }));
        const result = await postcodesIoLookup("SW1A 2AA", "10 Downing Street");
        expect(result).toEqual({
            displayName: "10 Downing Street, St James's, Westminster, London, England",
            lat: 51.5034,
            lon: -0.1276,
            address: {
                postcode: "SW1A 2AA",
                countryCode: "gb",
                line1: "10 Downing Street",
                city: "Westminster",
                stateOrRegion: "London",
            },
        });
        const calledUrl = String(fetchMock.mock.calls[0][0]);
        expect(calledUrl).toContain("api.postcodes.io/postcodes/SW1A%202AA");
    });
});
//# sourceMappingURL=postcodesIo.test.js.map