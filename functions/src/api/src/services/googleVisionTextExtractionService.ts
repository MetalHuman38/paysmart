import { ImageAnnotatorClient } from "@google-cloud/vision";

const CANDIDATE_NAME_KEYWORDS = [
  "name",
  "given names",
  "given name",
  "surname",
  "family name",
  "last name",
  "first name",
];

const NON_NAME_LINE_KEYWORDS = [
  "passport",
  "nationality",
  "date of birth",
  "birth",
  "sex",
  "expiry",
  "expiration",
  "issuing",
  "authority",
  "country",
  "document",
  "signature",
  "address",
  "number",
];

export type IdentityVisionExtraction = {
  fullText: string;
  candidateFullName?: string;
  provider: string;
};

export class GoogleVisionTextExtractionService {
  constructor(
    private readonly client: ImageAnnotatorClient = new ImageAnnotatorClient(),
    private readonly enabled = true
  ) {}

  async extract(
    imageBytes: Buffer,
    mimeType: string
  ): Promise<IdentityVisionExtraction> {
    if (!this.enabled) {
      throw new Error("IDENTITY_OCR_NOT_CONFIGURED");
    }
    if (!imageBytes || imageBytes.byteLength === 0) {
      throw new Error("Document payload is empty");
    }
    if (!mimeType || mimeType.trim().length === 0) {
      throw new Error("Missing document mimeType");
    }

    const [result] = await this.client.documentTextDetection({
      image: {
        content: imageBytes,
      },
    });

    const fullText = (
      result.fullTextAnnotation?.text ||
      result.textAnnotations?.[0]?.description ||
      ""
    ).trim();
    if (!fullText) {
      throw new Error("Vision OCR returned empty text");
    }

    return {
      fullText,
      candidateFullName: extractCandidateFullNameFromVisionText(fullText),
      provider: "google_cloud_vision_document_text_detection_v1",
    };
  }
}

export function extractCandidateFullNameFromVisionText(
  fullText: string
): string | undefined {
  const lines = fullText
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean);
  if (lines.length === 0) return undefined;

  const labeledCandidate = extractLabeledName(lines);
  if (labeledCandidate) {
    return labeledCandidate;
  }

  const mrzCandidate = extractNameFromMrz(lines);
  if (mrzCandidate) {
    return mrzCandidate;
  }

  const fallbackCandidate = extractLikelyNameLine(lines);
  return fallbackCandidate;
}

function extractLabeledName(lines: string[]): string | undefined {
  for (let i = 0; i < lines.length; i += 1) {
    const line = lines[i];
    const normalizedLine = line.toLowerCase();

    for (const keyword of CANDIDATE_NAME_KEYWORDS) {
      const directPattern = new RegExp(
        `^${escapeRegex(keyword)}\\s*[:\\-]?\\s+(.+)$`,
        "i"
      );
      const directMatch = line.match(directPattern);
      if (directMatch?.[1]) {
        const cleaned = cleanCandidateName(directMatch[1]);
        if (isValidCandidateName(cleaned)) {
          return cleaned;
        }
      }

      if (normalizedLine === keyword && i + 1 < lines.length) {
        const cleaned = cleanCandidateName(lines[i + 1]);
        if (isValidCandidateName(cleaned)) {
          return cleaned;
        }
      }
    }
  }

  return undefined;
}

function extractNameFromMrz(lines: string[]): string | undefined {
  for (const line of lines) {
    if (!line.includes("<<")) continue;
    const upper = line.toUpperCase().replace(/\s+/g, "");
    if (!upper.startsWith("P<")) continue;

    const afterCountry = upper.slice(5);
    const segments = afterCountry.split("<<");
    if (segments.length < 2) continue;
    const surname = segments[0].replace(/<+/g, " ").trim();
    const given = segments
      .slice(1)
      .join(" ")
      .replace(/<+/g, " ")
      .trim();
    const combined = cleanCandidateName(`${given} ${surname}`);
    if (isValidCandidateName(combined)) {
      return combined;
    }
  }

  return undefined;
}

function extractLikelyNameLine(lines: string[]): string | undefined {
  const ranked = lines
    .map((line) => cleanCandidateName(line))
    .filter((line) => isValidCandidateName(line))
    .filter((line) => !looksLikeNonNameLine(line))
    .sort((a, b) => scoreCandidateName(b) - scoreCandidateName(a));

  return ranked[0];
}

function scoreCandidateName(value: string): number {
  const tokens = value.split(/\s+/).filter(Boolean);
  const uppercaseTokens = tokens.filter((token) => token === token.toUpperCase());
  return tokens.length * 2 + uppercaseTokens.length;
}

function looksLikeNonNameLine(value: string): boolean {
  const normalized = value.toLowerCase();
  return NON_NAME_LINE_KEYWORDS.some((keyword) => normalized.includes(keyword));
}

function isValidCandidateName(value: string): boolean {
  if (!value) return false;
  if (value.length < 4 || value.length > 100) return false;
  const tokens = value.split(/\s+/).filter(Boolean);
  if (tokens.length < 2 || tokens.length > 6) return false;
  if (tokens.some((token) => token.length < 2)) return false;
  if (/^\d+$/.test(value)) return false;
  return true;
}

function cleanCandidateName(value: string): string {
  return value
    .replace(/[|\\/_~`]+/g, " ")
    .replace(/[^\p{L}\p{N}\s'.-]+/gu, " ")
    .replace(/\s+/g, " ")
    .trim();
}

function escapeRegex(value: string): string {
  return value.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}
