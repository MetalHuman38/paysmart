import { buildFullAddressWithHouse, normalizeOptionalText, normalizePostCode, } from "./normalization.js";
export function toResolvedResult(candidate, house, source) {
    const houseInfo = house.trim();
    const postCode = candidate.address.postcode.toUpperCase();
    const countryCode = candidate.address.countryCode.toUpperCase();
    const line1 = normalizeOptionalText(candidate.address.line1) ??
        normalizeOptionalText(houseInfo) ??
        candidate.displayName;
    const fullAddressWithHouse = buildFullAddressWithHouse(candidate.displayName, houseInfo);
    return {
        status: "OK",
        decision: "REVIEW",
        decisionReasons: ["fallback_geocode_only"],
        fullAddress: candidate.displayName,
        lat: candidate.lat,
        lng: candidate.lon,
        postCode,
        houseInfo,
        countryCode,
        fullAddressWithHouse,
        line1,
        line2: normalizeOptionalText(candidate.address.line2),
        city: normalizeOptionalText(candidate.address.city),
        stateOrRegion: normalizeOptionalText(candidate.address.stateOrRegion),
        source,
        mapPin: {
            lat: candidate.lat,
            lng: candidate.lon,
            label: fullAddressWithHouse,
            placeId: normalizeOptionalText(candidate.placeId),
        },
    };
}
export function classifyAddressValidationVerdict(input) {
    const verdict = input.verdict;
    const reasons = [];
    if (verdict?.addressComplete !== true) {
        reasons.push("address_incomplete");
    }
    const countryMatches = isCountryMatch(input.requestedCountryCode, input.resolvedCountryCode);
    if (!countryMatches) {
        reasons.push("country_mismatch");
    }
    const postCodeMatches = isPostcodeMatch(input.requestedPostCode, input.resolvedPostCode);
    if (!postCodeMatches) {
        reasons.push("postcode_mismatch");
    }
    const hasPremiseLevelGranularity = hasPremiseGranularity(verdict?.validationGranularity) &&
        hasPremiseGranularity(verdict?.geocodeGranularity);
    if (!hasPremiseLevelGranularity) {
        reasons.push("granularity_not_premise");
    }
    if (verdict?.hasInferredComponents) {
        reasons.push("inferred_components");
    }
    if (verdict?.hasUnconfirmedComponents) {
        reasons.push("unconfirmed_components");
    }
    if (verdict?.hasReplacedComponents) {
        reasons.push("replaced_components");
    }
    if (!countryMatches || !postCodeMatches || verdict?.addressComplete !== true) {
        return { decision: "REJECT", reasons };
    }
    if (hasPremiseLevelGranularity && reasons.length === 0) {
        return { decision: "VERIFIED", reasons: [] };
    }
    return { decision: "REVIEW", reasons };
}
export function toMapPinBounds(bounds) {
    const low = bounds?.low;
    const high = bounds?.high;
    if (typeof low?.latitude !== "number" ||
        !Number.isFinite(low.latitude) ||
        typeof low.longitude !== "number" ||
        !Number.isFinite(low.longitude) ||
        typeof high?.latitude !== "number" ||
        !Number.isFinite(high.latitude) ||
        typeof high.longitude !== "number" ||
        !Number.isFinite(high.longitude)) {
        return undefined;
    }
    return {
        low: {
            latitude: low.latitude,
            longitude: low.longitude,
        },
        high: {
            latitude: high.latitude,
            longitude: high.longitude,
        },
    };
}
function hasPremiseGranularity(granularity) {
    const normalized = normalizeOptionalText(granularity)?.toUpperCase();
    return normalized === "PREMISE" || normalized === "SUB_PREMISE";
}
function isCountryMatch(requestedCountryCode, resolvedCountryCode) {
    const requested = normalizeOptionalText(requestedCountryCode)?.toUpperCase();
    const resolved = normalizeOptionalText(resolvedCountryCode)?.toUpperCase();
    if (!requested || !resolved) {
        return true;
    }
    return requested === resolved;
}
function isPostcodeMatch(requestedPostCode, resolvedPostCode) {
    const requested = normalizePostCode(requestedPostCode);
    const resolved = normalizePostCode(resolvedPostCode);
    if (!requested || !resolved) {
        return true;
    }
    return requested.startsWith(resolved) || resolved.startsWith(requested);
}
//# sourceMappingURL=result.js.map