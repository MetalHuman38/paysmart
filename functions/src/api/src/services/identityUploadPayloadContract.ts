const HEADER_LENGTH_BYTES = 4;
export const IDENTITY_UPLOAD_PAYLOAD_CONTRACT_VERSION = "identity-upload-v2";
type ClientInfo = {
  firstName: string;
  middleName?: string;
  lastName: string;
  fullName: string;
  email: string;
  dateOfBirth: string;
  countryIso2: string;
};
type ExtractionInfo = {
  candidateFullName?: string;
  provider?: string;
};
export type ParsedIdentityUploadPayload = {
  contractVersion: string;
  documentType: string;
  contentType: string;
  documentBytes: Buffer;
  clientInfo: ClientInfo;
  extraction: ExtractionInfo;
};
export type NameMatchResult = {
  isMatch: boolean;
  score: number;
};
export function parseIdentityUploadPayload(
  decryptedPayload: Buffer
): ParsedIdentityUploadPayload {
  if (decryptedPayload.length <= HEADER_LENGTH_BYTES) {
    throw new Error("Legacy identity payload does not include client info contract");
  }
  const headerLength = decryptedPayload.readUInt32BE(0);
  if (
    !Number.isFinite(headerLength) ||
    headerLength <= 0 ||
    headerLength > decryptedPayload.length - HEADER_LENGTH_BYTES
  ) {
    throw new Error("Client payload header is malformed");
  }
  const headerBytes = decryptedPayload.subarray(
    HEADER_LENGTH_BYTES,
    HEADER_LENGTH_BYTES + headerLength
  );
  const documentBytes = decryptedPayload.subarray(HEADER_LENGTH_BYTES + headerLength);
  if (documentBytes.length <= 0) {
    throw new Error("Client payload document body is empty");
  }
  let header: any;
  try {
    header = JSON.parse(headerBytes.toString("utf8"));
  } catch {
    throw new Error("Client payload header is not valid JSON");
  }
  const contractVersion = readString(header, "contractVersion");
  if (contractVersion !== IDENTITY_UPLOAD_PAYLOAD_CONTRACT_VERSION) {
    throw new Error("Unsupported client payload contract version");
  }
  const document = readObject(header, "document");
  const clientInfo = readObject(header, "clientInfo");
  const extraction = (header.extraction ?? {}) as Record<string, unknown>;
  return {
    contractVersion,
    documentType: readString(document, "documentType"),
    contentType: readString(document, "contentType"),
    documentBytes,
    clientInfo: {
      firstName: readString(clientInfo, "firstName"),
      middleName: readOptionalString(clientInfo, "middleName"),
      lastName: readString(clientInfo, "lastName"),
      fullName: readString(clientInfo, "fullName"),
      email: readString(clientInfo, "email"),
      dateOfBirth: readString(clientInfo, "dateOfBirth"),
      countryIso2: readString(clientInfo, "countryIso2"),
    },
    extraction: {
      candidateFullName: readOptionalString(extraction, "candidateFullName"),
      provider: readOptionalString(extraction, "provider"),
    },
  };
}
export function evaluateNameMatch(
  expectedFullName: string,
  extractedCandidateName: string
): NameMatchResult {
  const expected = tokenizeName(expectedFullName);
  const candidate = tokenizeName(extractedCandidateName);
  if (expected.size === 0 || candidate.size === 0) {
    return { isMatch: false, score: 0 };
  }
  let overlap = 0;
  for (const token of expected) {
    if (candidate.has(token)) overlap += 1;
  }
  const score = overlap / expected.size;
  const threshold = Math.min(2, expected.size);
  const isMatch = overlap >= threshold || score >= 0.6;
  return { isMatch, score: Number(score.toFixed(3)) };
}
function tokenizeName(value: string): Set<string> {
  return new Set(
    value
      .trim()
      .toLowerCase()
      .replace(/[^a-z0-9 ]/g, " ")
      .split(/\s+/)
      .filter((token) => token.length >= 2)
  );
}
function readObject(
  source: Record<string, unknown>,
  key: string
): Record<string, unknown> {
  const value = source[key];
  if (!value || typeof value !== "object") {
    throw new Error(`Client payload is missing ${key}`);
  }
  return value as Record<string, unknown>;
}
function readString(source: Record<string, unknown>, key: string): string {
  const value = source[key];
  if (typeof value !== "string" || value.trim().length === 0) {
    throw new Error(`Client payload is missing ${key}`);
  }
  return value.trim();
}
function readOptionalString(
  source: Record<string, unknown>,
  key: string
): string | undefined {
  const value = source[key];
  if (typeof value !== "string") return undefined;
  const trimmed = value.trim();
  return trimmed.length > 0 ? trimmed : undefined;
}
