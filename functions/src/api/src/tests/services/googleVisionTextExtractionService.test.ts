import { describe, expect, it, vi } from "vitest";

vi.mock("@google-cloud/vision", () => ({
  ImageAnnotatorClient: class {},
}));
import { extractCandidateFullNameFromVisionText } from "../../services/googleVisionTextExtractionService.js";

describe("extractCandidateFullNameFromVisionText", () => {
  it("extracts name from labeled field", () => {
    const fullText = `
      PASSPORT
      Name: John Michael Smith
      Nationality: British
    `;

    expect(extractCandidateFullNameFromVisionText(fullText)).toBe(
      "John Michael Smith"
    );
  });

  it("extracts name from MRZ line", () => {
    const fullText = `
      P<GBRSMITH<<JOHN<PAUL<<<<<<<<<<<<<<<<<<<<
      1234567890GBR9001011M3001012<<<<<<<<<<<<
    `;

    expect(extractCandidateFullNameFromVisionText(fullText)).toBe(
      "JOHN PAUL SMITH"
    );
  });

  it("returns undefined when no plausible name candidate exists", () => {
    const fullText = `
      DOCUMENT NUMBER 123456789
      DATE OF BIRTH 01 JAN 1990
      EXPIRY DATE 01 JAN 2030
    `;

    expect(extractCandidateFullNameFromVisionText(fullText)).toBeUndefined();
  });
});
