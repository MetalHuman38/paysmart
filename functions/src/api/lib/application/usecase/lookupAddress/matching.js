import { normalizePostCode } from "./normalization.js";
export function pickCandidate(candidates, postcode, country) {
    const postcodeNormalized = normalizePostCode(postcode);
    const countryNormalized = country.toLowerCase();
    for (const candidate of candidates) {
        const address = candidate.address;
        if (normalizePostCode(address.postcode) === postcodeNormalized &&
            address.countryCode.toLowerCase() === countryNormalized) {
            return candidate;
        }
    }
    return null;
}
//# sourceMappingURL=matching.js.map