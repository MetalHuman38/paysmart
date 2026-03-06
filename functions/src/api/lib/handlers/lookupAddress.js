import { LookupAddress } from "../application/usecase/LookupAddress.js";
let lookupAddressUseCase = null;
function getLookupAddressUseCase() {
    if (!lookupAddressUseCase) {
        lookupAddressUseCase = new LookupAddress();
    }
    return lookupAddressUseCase;
}
function readString(value, fallback = "") {
    if (typeof value !== "string") {
        return fallback;
    }
    return value.trim();
}
function readOptionalNumber(value) {
    if (typeof value === "number" && Number.isFinite(value)) {
        return value;
    }
    if (typeof value === "string") {
        const parsed = Number(value);
        if (Number.isFinite(parsed)) {
            return parsed;
        }
    }
    return undefined;
}
export async function lookupAddressHandler(req, res) {
    try {
        const body = (req.body ?? {});
        const line1 = readString(body.line1 || body.house);
        const city = readString(body.city);
        const stateOrRegion = readString(body.stateOrRegion);
        const postcode = readString(body.postalCode || body.postcode);
        const country = readString(body.country, "gb").toLowerCase();
        const lat = readOptionalNumber(body.lat);
        const lng = readOptionalNumber(body.lng);
        if (![line1, city, stateOrRegion, postcode].some((value) => value.length > 0)) {
            return res.status(400).json({ error: "at least one address field is required" });
        }
        const resolved = await getLookupAddressUseCase().execute({
            line1,
            city,
            stateOrRegion,
            postcode,
            country,
            lat,
            lng,
        });
        if (!resolved) {
            return res.status(404).json({ error: "No results found" });
        }
        return res.status(200).json(resolved);
    }
    catch (error) {
        console.error("lookupAddressHandler failed", error);
        return res.status(500).json({ error: "Unable to resolve address" });
    }
}
//# sourceMappingURL=lookupAddress.js.map