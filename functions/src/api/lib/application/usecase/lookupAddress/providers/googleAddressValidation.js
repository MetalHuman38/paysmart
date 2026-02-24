import { GOOGLE_ADDRESS_VALIDATION_ENDPOINT } from "../constants.js";
import { postJson } from "../http.js";
import { buildFullAddressWithHouse, normalizeCountryCode, normalizeOptionalText, } from "../normalization.js";
import { classifyAddressValidationVerdict, toMapPinBounds, } from "../result.js";
export async function googleAddressValidationLookup(input) {
    const apiKey = process.env.GOOGLE_ADDRESS_VALIDATION_KEY;
    if (!apiKey) {
        return null;
    }
    const countryCode = normalizeCountryCode(input.country).toUpperCase();
    const postcode = input.postcode.trim();
    const house = input.house.trim();
    const addressLine = [house, postcode].filter((value) => value.length > 0).join(" ");
    if (!addressLine) {
        return null;
    }
    const url = `${GOOGLE_ADDRESS_VALIDATION_ENDPOINT}?key=${encodeURIComponent(apiKey)}`;
    const payload = await postJson(url, {
        address: {
            regionCode: countryCode,
            postalCode: postcode,
            addressLines: [addressLine],
        },
        enableUspsCass: countryCode === "US",
    }, 8_000).catch(() => null);
    if (!payload?.result) {
        return null;
    }
    const result = payload.result;
    const geocode = result.geocode?.location;
    if (typeof geocode?.latitude !== "number" ||
        !Number.isFinite(geocode.latitude) ||
        typeof geocode.longitude !== "number" ||
        !Number.isFinite(geocode.longitude)) {
        return null;
    }
    const postalAddress = result.address?.postalAddress;
    const fullAddress = normalizeOptionalText(result.address?.formattedAddress) ?? addressLine;
    const resolvedCountryCode = normalizeOptionalText(postalAddress?.regionCode)?.toUpperCase() ?? countryCode;
    const resolvedPostCode = normalizeOptionalText(postalAddress?.postalCode) ?? postcode;
    const line1 = normalizeOptionalText(postalAddress?.addressLines?.[0]) ??
        normalizeOptionalText(house) ??
        fullAddress;
    const line2 = normalizeOptionalText(postalAddress?.addressLines?.[1]);
    const city = normalizeOptionalText(postalAddress?.locality);
    const stateOrRegion = normalizeOptionalText(postalAddress?.administrativeArea);
    const fullAddressWithHouse = buildFullAddressWithHouse(fullAddress, house);
    const classification = classifyAddressValidationVerdict({
        verdict: result.verdict,
        requestedCountryCode: countryCode,
        resolvedCountryCode,
        requestedPostCode: postcode,
        resolvedPostCode,
    });
    return {
        status: "OK",
        decision: classification.decision,
        decisionReasons: classification.reasons,
        fullAddress,
        lat: geocode.latitude,
        lng: geocode.longitude,
        postCode: resolvedPostCode.toUpperCase(),
        houseInfo: house,
        countryCode: resolvedCountryCode,
        fullAddressWithHouse,
        line1,
        line2,
        city,
        stateOrRegion,
        source: "google_address_validation",
        providerResponseId: normalizeOptionalText(payload.responseId),
        mapPin: {
            lat: geocode.latitude,
            lng: geocode.longitude,
            label: fullAddressWithHouse,
            placeId: normalizeOptionalText(result.geocode?.placeId),
            plusCode: normalizeOptionalText(result.geocode?.plusCode?.globalCode),
            bounds: toMapPinBounds(result.geocode?.bounds),
        },
    };
}
//# sourceMappingURL=googleAddressValidation.js.map