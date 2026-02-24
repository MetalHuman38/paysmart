import { fetchJson } from "../http.js";
import { normalizeOptionalText } from "../normalization.js";
export async function postcodesIoLookup(postcode, house) {
    const url = `https://api.postcodes.io/postcodes/${encodeURIComponent(postcode)}`;
    const data = await fetchJson(url, {}, 6_000).catch(() => null);
    if (!data?.result || data.status !== 200) {
        return null;
    }
    const result = data.result;
    const latitude = result.latitude;
    const longitude = result.longitude;
    if (typeof latitude !== "number" || typeof longitude !== "number") {
        return null;
    }
    const areas = [
        normalizeOptionalText(result.admin_ward),
        normalizeOptionalText(result.admin_district),
        normalizeOptionalText(result.region),
        normalizeOptionalText(result.country),
    ].filter((value) => Boolean(value));
    const line1 = normalizeOptionalText(house) ?? areas[0] ?? postcode;
    const city = normalizeOptionalText(result.admin_district) ??
        normalizeOptionalText(result.admin_ward);
    const stateOrRegion = normalizeOptionalText(result.region);
    const displayName = [line1, ...areas].join(", ");
    return {
        displayName,
        lat: latitude,
        lon: longitude,
        address: {
            postcode: normalizeOptionalText(result.postcode) ?? postcode,
            countryCode: "gb",
            line1,
            city,
            stateOrRegion,
        },
    };
}
//# sourceMappingURL=postcodesIo.js.map