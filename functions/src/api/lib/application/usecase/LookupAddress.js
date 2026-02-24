import { pickCandidate } from "./lookupAddress/matching.js";
import { normalizeCountryCode } from "./lookupAddress/normalization.js";
import { googleAddressValidationLookup } from "./lookupAddress/providers/googleAddressValidation.js";
import { googleGeocode } from "./lookupAddress/providers/googleGeocode.js";
import { nominatimSearch } from "./lookupAddress/providers/nominatim.js";
import { postcodesIoLookup } from "./lookupAddress/providers/postcodesIo.js";
import { toResolvedResult } from "./lookupAddress/result.js";
export class LookupAddress {
    async execute(input) {
        const house = input.house.trim();
        const postcode = input.postcode.trim();
        const country = normalizeCountryCode(input.country);
        if (!postcode) {
            throw new Error("postcode is required");
        }
        const addressValidationResult = await googleAddressValidationLookup({
            house,
            postcode,
            country,
        });
        if (addressValidationResult) {
            return addressValidationResult;
        }
        if (country === "gb") {
            const ukCandidate = await postcodesIoLookup(postcode, house);
            if (ukCandidate) {
                return toResolvedResult(ukCandidate, house, "postcodes_io");
            }
        }
        const query = [house, postcode].filter((value) => value.length > 0).join(" ");
        const nominatim = await nominatimSearch(query, country, input.lat, input.lng);
        const nominatimBest = pickCandidate(nominatim, postcode, country);
        if (nominatimBest) {
            return toResolvedResult(nominatimBest, house, "nominatim");
        }
        const google = await googleGeocode(query, country);
        const googleBest = pickCandidate(google, postcode, country);
        if (googleBest) {
            return toResolvedResult(googleBest, house, "google");
        }
        return null;
    }
}
//# sourceMappingURL=LookupAddress.js.map