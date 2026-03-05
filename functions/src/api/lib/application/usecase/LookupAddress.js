import { pickCandidate } from "./lookupAddress/matching.js";
import { normalizeCountryCode } from "./lookupAddress/normalization.js";
import { googleAddressValidationLookup } from "./lookupAddress/providers/googleAddressValidation.js";
import { googleGeocode } from "./lookupAddress/providers/googleGeocode.js";
import { nominatimSearch } from "./lookupAddress/providers/nominatim.js";
import { postcodesIoLookup } from "./lookupAddress/providers/postcodesIo.js";
import { toResolvedResult } from "./lookupAddress/result.js";
export class LookupAddress {
    async execute(input) {
        const line1 = input.line1.trim();
        const city = input.city.trim();
        const stateOrRegion = input.stateOrRegion.trim();
        const postcode = input.postcode.trim();
        const country = normalizeCountryCode(input.country);
        if (![line1, city, stateOrRegion, postcode].some((value) => value.length > 0)) {
            throw new Error("at least one address field is required");
        }
        const addressValidationResult = await googleAddressValidationLookup({
            line1,
            city,
            stateOrRegion,
            postcode,
            country,
        });
        if (addressValidationResult) {
            return addressValidationResult;
        }
        if (country === "gb" && postcode) {
            const ukCandidate = await postcodesIoLookup(postcode, line1);
            if (ukCandidate) {
                return toResolvedResult(ukCandidate, line1, "postcodes_io");
            }
        }
        const query = [line1, city, stateOrRegion, postcode]
            .filter((value) => value.length > 0)
            .join(", ");
        const nominatim = await nominatimSearch(query, country, input.lat, input.lng);
        const nominatimBest = pickCandidate(nominatim, {
            line1,
            city,
            stateOrRegion,
            postcode,
            country,
        });
        if (nominatimBest) {
            return toResolvedResult(nominatimBest, line1, "nominatim");
        }
        const google = await googleGeocode(query, country);
        const googleBest = pickCandidate(google, {
            line1,
            city,
            stateOrRegion,
            postcode,
            country,
        });
        if (googleBest) {
            return toResolvedResult(googleBest, line1, "google");
        }
        return null;
    }
}
//# sourceMappingURL=LookupAddress.js.map