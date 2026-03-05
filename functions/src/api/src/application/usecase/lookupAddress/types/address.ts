export type AddressLookupInput = {
  line1: string;
  city: string;
  stateOrRegion: string;
  postcode: string;
  country: string;
  lat?: number;
  lng?: number;
};

export type AddressDecision = "VERIFIED" | "REVIEW" | "REJECT";

type MapPinCoordinate = {
  latitude: number;
  longitude: number;
};

export type MapPinPayload = {
  lat: number;
  lng: number;
  label: string;
  placeId?: string;
  plusCode?: string;
  bounds?: {
    low: MapPinCoordinate;
    high: MapPinCoordinate;
  };
};

export type AddressCandidate = {
  displayName: string;
  lat: number;
  lon: number;
  placeId?: string;
  address: {
    postcode?: string;
    countryCode: string;
    line1?: string;
    line2?: string;
    city?: string;
    stateOrRegion?: string;
  };
};

export type AddressLookupResult = {
  status: "OK";
  decision: AddressDecision;
  decisionReasons: string[];
  fullAddress: string;
  lat: number;
  lng: number;
  postCode: string;
  houseInfo: string;
  countryCode: string;
  fullAddressWithHouse: string;
  line1: string;
  line2?: string;
  city?: string;
  stateOrRegion?: string;
  source: "google_address_validation" | "postcodes_io" | "nominatim" | "google";
  providerResponseId?: string;
  mapPin: MapPinPayload;
};
