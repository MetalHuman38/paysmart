import { AddressCandidate } from "../types/address.js";
import { NominatimSearchRow } from "../types/addressLookupProvider.js";
import { NOMINATIM_USER_AGENT } from "../constants.js";
import { fetchJson } from "../http.js";
import { normalizeOptionalText } from "../normalization.js";

export async function nominatimSearch(
  query: string,
  country: string,
  lat?: number,
  lng?: number
): Promise<AddressCandidate[]> {
  if (!query.trim()) {
    return [];
  }

  const params = new URLSearchParams();
  params.set("q", query);
  params.set("format", "json");
  params.set("addressdetails", "1");
  params.set("countrycodes", country);
  params.set("limit", "5");

  if (typeof lat === "number" && typeof lng === "number") {
    const north = lat + 0.1;
    const south = lat - 0.1;
    const east = lng + 0.1;
    const west = lng - 0.1;
    params.set("viewbox", `${west},${north},${east},${south}`);
    params.set("bounded", "1");
  }

  const url = `https://nominatim.openstreetmap.org/search?${params.toString()}`;
  const rows = await fetchJson<NominatimSearchRow[]>(
    url,
    { headers: { "User-Agent": NOMINATIM_USER_AGENT } },
    8_000
  ).catch(() => []);

  const candidates: AddressCandidate[] = [];
  for (const row of rows) {
    const latValue = Number(row.lat);
    const lonValue = Number(row.lon);
    const address = row.address;
    const postcode = normalizeOptionalText(address?.postcode);
    const countryCode = normalizeOptionalText(address?.country_code)?.toLowerCase();

    if (
      !row.display_name ||
      !Number.isFinite(latValue) ||
      !Number.isFinite(lonValue) ||
      !postcode ||
      !countryCode
    ) {
      continue;
    }

    const houseNumber = normalizeOptionalText(address?.house_number);
    const road = normalizeOptionalText(address?.road);
    const line1 = [houseNumber, road].filter((value) => value != null).join(" ").trim();
    const line2 = normalizeOptionalText(address?.suburb);
    const city =
      normalizeOptionalText(address?.city) ??
      normalizeOptionalText(address?.town) ??
      normalizeOptionalText(address?.village) ??
      normalizeOptionalText(address?.county);
    const stateOrRegion = normalizeOptionalText(address?.state);

    candidates.push({
      displayName: row.display_name,
      lat: latValue,
      lon: lonValue,
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
