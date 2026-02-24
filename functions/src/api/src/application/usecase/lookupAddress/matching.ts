import { AddressCandidate } from "./types/address.js";
import { normalizePostCode } from "./normalization.js";

export function pickCandidate(
  candidates: AddressCandidate[],
  postcode: string,
  country: string
): AddressCandidate | null {
  const postcodeNormalized = normalizePostCode(postcode);
  const countryNormalized = country.toLowerCase();

  for (const candidate of candidates) {
    const address = candidate.address;
    if (
      normalizePostCode(address.postcode) === postcodeNormalized &&
      address.countryCode.toLowerCase() === countryNormalized
    ) {
      return candidate;
    }
  }

  return null;
}
