export function resolveFlutterwavePaymentsConfigErrorCode(
  message: string
): string | null {
  if (message.includes("FLUTTERWAVE_SECRET_KEY is not configured")) {
    return "MISSING_FLUTTERWAVE_SECRET_KEY";
  }
  if (message.includes("FLUTTERWAVE_PUBLIC_KEY is not configured")) {
    return "MISSING_FLUTTERWAVE_PUBLIC_KEY";
  }
  if (message.includes("FLUTTERWAVE_NOT_IMPLEMENTED_FLW_001")) {
    return "FLUTTERWAVE_NOT_IMPLEMENTED";
  }
  return null;
}
