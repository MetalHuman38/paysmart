import { Request, Response } from "express";
import { LookupAddress } from "../application/usecase/LookupAddress.js";

const lookupAddressUseCase = new LookupAddress();

type LookupAddressPayload = {
  house?: unknown;
  line1?: unknown;
  city?: unknown;
  stateOrRegion?: unknown;
  postcode?: unknown;
  postalCode?: unknown;
  country?: unknown;
  lat?: unknown;
  lng?: unknown;
};

function readString(value: unknown, fallback = ""): string {
  if (typeof value !== "string") {
    return fallback;
  }
  return value.trim();
}

function readOptionalNumber(value: unknown): number | undefined {
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

export async function lookupAddressHandler(req: Request, res: Response) {
  try {
    const body = (req.body ?? {}) as LookupAddressPayload;
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

    const resolved = await lookupAddressUseCase.execute({
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
  } catch (error) {
    console.error("lookupAddressHandler failed", error);
    return res.status(500).json({ error: "Unable to resolve address" });
  }
}
