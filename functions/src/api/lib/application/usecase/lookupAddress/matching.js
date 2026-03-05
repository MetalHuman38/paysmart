import { normalizePostCode } from "./normalization.js";
export function pickCandidate(candidates, input) {
    const countryNormalized = normalizeAlpha(input.country);
    const postcodeNormalized = normalizePostCode(input.postcode);
    const cityNormalized = normalizeText(input.city);
    const stateNormalized = normalizeText(input.stateOrRegion);
    const line1Tokens = tokenize(input.line1);
    let bestCandidate = null;
    let bestScore = Number.NEGATIVE_INFINITY;
    for (const candidate of candidates) {
        const address = candidate.address;
        const candidateCountry = normalizeAlpha(address.countryCode);
        if (countryNormalized && candidateCountry && candidateCountry !== countryNormalized) {
            continue;
        }
        let score = 0;
        if (countryNormalized && candidateCountry === countryNormalized) {
            score += 5;
        }
        else if (!candidateCountry) {
            score += 1;
        }
        const candidatePostcode = normalizePostCode(address.postcode ?? "");
        if (postcodeNormalized) {
            if (candidatePostcode &&
                (candidatePostcode === postcodeNormalized ||
                    candidatePostcode.startsWith(postcodeNormalized) ||
                    postcodeNormalized.startsWith(candidatePostcode))) {
                score += 4;
            }
            else if (candidatePostcode) {
                score -= 2;
            }
        }
        else if (candidatePostcode) {
            score += 1;
        }
        const candidateCity = normalizeText(address.city);
        if (cityNormalized && candidateCity && cityNormalized === candidateCity) {
            score += 2;
        }
        const candidateState = normalizeText(address.stateOrRegion);
        if (stateNormalized && candidateState && stateNormalized === candidateState) {
            score += 1;
        }
        const candidateLine = normalizeText(address.line1) ?? normalizeText(candidate.displayName) ?? "";
        if (line1Tokens.length > 0) {
            const tokenHits = line1Tokens.filter((token) => candidateLine.includes(token)).length;
            if (tokenHits > 0) {
                score += Math.min(3, tokenHits);
            }
        }
        if (score > bestScore) {
            bestScore = score;
            bestCandidate = candidate;
        }
    }
    return bestCandidate;
}
function normalizeAlpha(value) {
    return (value ?? "").trim().toLowerCase();
}
function normalizeText(value) {
    const normalized = (value ?? "")
        .trim()
        .toLowerCase()
        .replace(/[^\p{L}\p{N}\s]/gu, " ")
        .replace(/\s+/g, " ");
    return normalized.length > 0 ? normalized : undefined;
}
function tokenize(value) {
    const normalized = normalizeText(value);
    if (!normalized) {
        return [];
    }
    return normalized
        .split(" ")
        .filter((token) => token.length >= 3);
}
//# sourceMappingURL=matching.js.map