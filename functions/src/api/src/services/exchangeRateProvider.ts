type ExchangeRateCacheEntry = {
  rate: number;
  expiresAtMs: number;
};

type ExchangeRateProviderOptions = {
  apiKey: string;
  cacheTtlMs: number;
  timeoutMs: number;
  upstreamBaseUrl: string;
};

export type RateFetchResult = {
  rate: number;
  source: "cache" | "upstream";
};

type ExchangeRateApiPayload = {
  result?: string;
  conversion_rate?: number;
};

export class ExchangeRateProvider {
  private readonly cache = new Map<string, ExchangeRateCacheEntry>();
  private readonly apiKey: string;
  private readonly cacheTtlMs: number;
  private readonly timeoutMs: number;
  private readonly upstreamBaseUrl: string;

  constructor(options: ExchangeRateProviderOptions) {
    this.apiKey = options.apiKey.trim();
    this.cacheTtlMs = sanitizePositive(options.cacheTtlMs, 60_000);
    this.timeoutMs = sanitizePositive(options.timeoutMs, 4_000);
    this.upstreamBaseUrl = (options.upstreamBaseUrl || "https://v6.exchangerate-api.com/v6")
      .trim()
      .replace(/\/+$/, "");
  }

  async getRate(sourceCurrency: string, targetCurrency: string): Promise<RateFetchResult> {
    const source = sourceCurrency.trim().toUpperCase();
    const target = targetCurrency.trim().toUpperCase();
    if (!source || !target) {
      throw new Error("Missing source or target currency");
    }

    if (!this.apiKey) {
      throw new Error("EXCHANGE_RATE_API_KEY is not configured");
    }

    const cacheKey = `${source}:${target}`;
    const cached = this.cache.get(cacheKey);
    const now = Date.now();
    if (cached && now < cached.expiresAtMs) {
      return {
        rate: cached.rate,
        source: "cache",
      };
    }

    const rate = await this.fetchRateWithRetry(source, target);
    if (!Number.isFinite(rate) || rate <= 0) {
      throw new Error("invalid_rate");
    }

    this.cache.set(cacheKey, {
      rate,
      expiresAtMs: now + this.cacheTtlMs,
    });

    return {
      rate,
      source: "upstream",
    };
  }

  private async fetchRateWithRetry(source: string, target: string): Promise<number> {
    const maxRetries = 2;
    let backoffMs = 250;

    for (let attempt = 0; attempt <= maxRetries; attempt += 1) {
      try {
        return await this.fetchRateOnce(source, target);
      } catch (error) {
        if (!isTransientError(error) || attempt >= maxRetries) {
          throw error;
        }

        await sleep(backoffMs);
        backoffMs *= 2;
      }
    }

    throw new Error("upstream_unreachable");
  }

  private async fetchRateOnce(source: string, target: string): Promise<number> {
    const url = `${this.upstreamBaseUrl}/${encodeURIComponent(this.apiKey)}/pair/${encodeURIComponent(source)}/${encodeURIComponent(target)}`;
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), this.timeoutMs);

    try {
      const response = await fetch(url, {
        method: "GET",
        signal: controller.signal,
      });

      if (response.status >= 500) {
        throw new Error(`provider status ${response.status}`);
      }

      const rawBody = await response.text();
      if (!response.ok) {
        throw new Error(`provider status ${response.status}: ${rawBody.slice(0, 200)}`);
      }

      const payload = safeJson(rawBody);
      if (payload.result !== "success" || !payload.conversion_rate || payload.conversion_rate <= 0) {
        throw new Error("bad_provider_payload");
      }

      return payload.conversion_rate;
    } catch (error) {
      if (isAbortError(error)) {
        throw new Error("timeout");
      }
      throw error;
    } finally {
      clearTimeout(timeout);
    }
  }
}

function safeJson(raw: string): ExchangeRateApiPayload {
  try {
    return JSON.parse(raw) as ExchangeRateApiPayload;
  } catch {
    return {};
  }
}

function sanitizePositive(value: number, fallback: number): number {
  if (!Number.isFinite(value) || value <= 0) {
    return fallback;
  }
  return Math.floor(value);
}

function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

function isTransientError(error: unknown): boolean {
  const message = error instanceof Error ? error.message.toLowerCase() : String(error).toLowerCase();
  return (
    message.includes("timeout") ||
    message.includes("abort") ||
    message.includes("provider status 5") ||
    message.includes("fetch failed") ||
    message.includes("network")
  );
}

function isAbortError(error: unknown): boolean {
  if (error instanceof Error) {
    return (
      error.name === "AbortError" ||
      error.message.toLowerCase().includes("aborted")
    );
  }
  return false;
}
