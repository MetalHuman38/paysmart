import { initDeps } from "../../dependencies.js";
import { ExchangeRateProvider } from "../../services/exchangeRateProvider.js";

let singleton:
  | {
      exchangeRateProvider: ExchangeRateProvider;
    }
  | null = null;

export function fxContainer() {
  if (singleton) {
    return singleton;
  }

  const { getConfig } = initDeps();
  const config = getConfig();
  singleton = {
    exchangeRateProvider: new ExchangeRateProvider({
      apiKey: config.exchangeRateApiKey,
      cacheTtlMs: config.exchangeRateCacheTtlMs,
      timeoutMs: config.exchangeRateTimeoutMs,
      upstreamBaseUrl: config.exchangeRateUpstreamBaseUrl,
    }),
  };

  return singleton;
}
