import { fetchJson } from "../http.js";
import { normalizeOptionalText } from "../normalization.js";
export async function googleGeocode(query, country) {
    const apiKey = process.env.GOOGLE_GEOCODE_KEY;
    if (!apiKey || !query.trim()) {
        return [];
    }
    const params = new URLSearchParams();
    params.set("address", query);
    if (country.trim().length > 0) {
        params.set("components", `country:${country}`);
    }
    params.set("key", apiKey);
    const url = `https://maps.googleapis.com/maps/api/geocode/json?${params.toString()}`;
    const payload = await fetchJson(url, {}, 8_000).catch(() => null);
    if (!payload?.results) {
        return [];
    }
    const candidates = [];
    for (const result of payload.results) {
        const displayName = normalizeOptionalText(result.formatted_address);
        const latValue = result.geometry?.location?.lat;
        const lonValue = result.geometry?.location?.lng;
        if (!displayName || typeof latValue !== "number" || typeof lonValue !== "number") {
            continue;
        }
        const components = new Map();
        for (const component of result.address_components ?? []) {
            const longName = normalizeOptionalText(component.long_name);
            if (!longName) {
                continue;
            }
            for (const type of component.types ?? []) {
                if (!components.has(type)) {
                    components.set(type, longName);
                }
            }
        }
        const postcode = normalizeOptionalText(components.get("postal_code"));
        const countryCode = normalizeOptionalText((result.address_components ?? [])
            .find((component) => component.types?.includes("country"))
            ?.short_name)?.toLowerCase() ?? normalizeOptionalText(components.get("country"))?.toLowerCase();
        if (!countryCode) {
            continue;
        }
        const line1 = [
            normalizeOptionalText(components.get("street_number")),
            normalizeOptionalText(components.get("route")),
        ]
            .filter((value) => value != null)
            .join(" ")
            .trim();
        const line2 = normalizeOptionalText(components.get("subpremise"));
        const city = normalizeOptionalText(components.get("locality")) ??
            normalizeOptionalText(components.get("postal_town"));
        const stateOrRegion = normalizeOptionalText(components.get("administrative_area_level_1"));
        candidates.push({
            displayName,
            lat: latValue,
            lon: lonValue,
            placeId: normalizeOptionalText(result.place_id),
            address: {
                postcode,
                countryCode,
                line1: normalizeOptionalText(line1),
                line2,
                city,
                stateOrRegion,
            },
        });
    }
    return candidates;
}
//# sourceMappingURL=googleGeocode.js.map