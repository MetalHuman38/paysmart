import { DEFAULT_COUNTRY_CODE } from "./constants.js";

export function normalizeCountryCode(value: string): string {
  const normalized = value.trim().toLowerCase();
  if (!normalized) {
    return DEFAULT_COUNTRY_CODE;
  }
  return normalized.slice(0, 2);
}

export function normalizePostCode(value: string): string {
  return value.replace(/\s+/g, "").toUpperCase();
}

export function normalizeOptionalText(value?: string): string | undefined {
  if (!value) {
    return undefined;
  }
  const normalized = value.trim();
  return normalized.length > 0 ? normalized : undefined;
}

export function buildFullAddressWithHouse(displayName: string, houseInfo: string): string {
  if (
    houseInfo.length > 0 &&
    !displayName.toLowerCase().startsWith(houseInfo.toLowerCase())
  ) {
    return `${houseInfo}, ${displayName}`;
  }
  return displayName;
}
