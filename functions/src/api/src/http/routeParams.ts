export function normalizeRouteParam(raw: unknown): string {
  return typeof raw === "string" ? raw.trim() : "";
}
