export type GoogleAddressValidationLookupInput = {
  house: string;
  postcode: string;
  country: string;
};

export type GoogleAddressValidationVerdict = {
  inputGranularity?: string;
  validationGranularity?: string;
  geocodeGranularity?: string;
  addressComplete?: boolean;
  hasInferredComponents?: boolean;
  hasUnconfirmedComponents?: boolean;
  hasReplacedComponents?: boolean;
};

export type GoogleAddressValidationBounds = {
  low?: {
    latitude?: number;
    longitude?: number;
  };
  high?: {
    latitude?: number;
    longitude?: number;
  };
};

export type GoogleAddressValidationResponse = {
  result?: {
    verdict?: GoogleAddressValidationVerdict;
    address?: {
      formattedAddress?: string;
      postalAddress?: {
        regionCode?: string;
        postalCode?: string;
        administrativeArea?: string;
        locality?: string;
        addressLines?: string[];
      };
    };
    geocode?: {
      location?: {
        latitude?: number;
        longitude?: number;
      };
      placeId?: string;
      plusCode?: {
        globalCode?: string;
      };
      bounds?: GoogleAddressValidationBounds;
    };
  };
  responseId?: string;
};

export type PostcodesIoLookupResponse = {
  status?: number;
  result?: {
    admin_ward?: string;
    admin_district?: string;
    region?: string;
    country?: string;
    postcode?: string;
    latitude?: number;
    longitude?: number;
  };
};

export type NominatimSearchRow = {
  display_name?: string;
  lat?: string;
  lon?: string;
  address?: {
    postcode?: string;
    country_code?: string;
    house_number?: string;
    road?: string;
    suburb?: string;
    city?: string;
    town?: string;
    village?: string;
    county?: string;
    state?: string;
  };
};

export type GoogleGeocodeResponse = {
  results?: Array<{
    formatted_address?: string;
    place_id?: string;
    geometry?: {
      location?: {
        lat?: number;
        lng?: number;
      };
    };
    address_components?: Array<{
      long_name?: string;
      short_name?: string;
      types?: string[];
    }>;
  }>;
};
