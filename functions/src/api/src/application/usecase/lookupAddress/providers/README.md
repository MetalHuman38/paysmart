# Lookup Address Providers

This folder contains provider-specific integrations used by `LookupAddress`.

## Provider contracts

- `googleAddressValidationLookup(input): Promise<AddressLookupResult | null>`
  - Returns a fully classified lookup result (`VERIFIED`, `REVIEW`, or `REJECT`) when Google Address Validation is available and returns usable geocode data.
  - Returns `null` when the API key is missing, payload is unusable, or the request fails.
  - Input includes `line1`, `city`, `stateOrRegion`, `postcode`, and `country`.
  - Reads `GOOGLE_ADDRESS_VALIDATION_KEY`.

- `postcodesIoLookup(postcode, house): Promise<AddressCandidate | null>`
  - Returns one UK candidate from `postcodes.io` when status/result/coordinates are present.
  - Returns `null` when payload is invalid or request fails.

- `nominatimSearch(query, country, lat?, lng?): Promise<AddressCandidate[]>`
  - Returns a list of mapped candidates that include display name, coordinates, and country code (postcode is optional).
  - Returns `[]` for blank query or request failure.
  - Uses bounded search when `lat/lng` are provided.

- `googleGeocode(query, country): Promise<AddressCandidate[]>`
  - Returns mapped Google Geocoding candidates with normalized address parts.
  - Returns `[]` when API key is missing, query is blank, payload is unusable, or request fails.
  - Reads `GOOGLE_GEOCODE_KEY`.

## Fallback contract (orchestration order)

`LookupAddress.execute(...)` applies providers in this order:

1. `googleAddressValidationLookup`
2. `postcodesIoLookup` (only when country is `gb`)
3. `nominatimSearch`
4. `googleGeocode`
5. `null` (no match)

### Selection rules for candidate providers

- Candidate lists from Nominatim/Google Geocode are filtered via `pickCandidate(...)`.
- `pickCandidate(...)` applies country-first scoring and then boosts matches on:
  - normalized postcode similarity (when provided)
  - city and state/region equality
  - line1 token overlap

### Result shaping for fallback candidates

- Candidates from `postcodesIoLookup`, `nominatimSearch`, and `googleGeocode` are converted with `toResolvedResult(...)`.
- Fallback results are always:
  - `decision: "REVIEW"`
  - `decisionReasons: ["fallback_geocode_only"]`
  - `source` set to one of `postcodes_io`, `nominatim`, or `google`

## Error-handling guarantees

- Provider network/parsing failures are absorbed inside provider modules and converted to `null`/`[]`.
- Provider errors do not bubble from `LookupAddress.execute(...)`.
- The only expected validation throw in the use case is when all address inputs are blank.

## Timeout expectations

- Google Address Validation: `8000ms`
- Nominatim: `8000ms`
- Google Geocode: `8000ms`
- Postcodes.io: `6000ms`
