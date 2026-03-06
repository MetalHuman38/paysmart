package net.metalbrain.paysmart.sandbox.config;

import java.util.Locale;

public record SandboxConfig(
        String stripeSecretKey,
        String defaultProductName,
        String defaultProductDescription,
        String defaultCurrency,
        long defaultUnitAmountMinor
) {
    public static SandboxConfig fromEnvironment() {
        String secretKey = required();
        String productName = optional("SANDBOX_PRODUCT_NAME", "PaySmart Investor Sandbox");
        String productDescription = optional(
                "SANDBOX_PRODUCT_DESCRIPTION",
                "Test-mode recurring plan for investor demos"
        );
        String currency = optional("SANDBOX_PRICE_CURRENCY", "gbp").trim().toLowerCase(Locale.US);
        long unitAmount = parseLong(optional("SANDBOX_PRICE_MINOR", "1200"));

        if (currency.length() != 3) {
            throw new IllegalArgumentException("SANDBOX_PRICE_CURRENCY must be a 3-letter code");
        }
        if (unitAmount <= 0) {
            throw new IllegalArgumentException("SANDBOX_PRICE_MINOR must be > 0");
        }

        return new SandboxConfig(
                secretKey,
                productName.trim(),
                productDescription.trim(),
                currency,
                unitAmount
        );
    }

    private static String required() {
        String value = readRuntimeValue("STRIPE_SECRET_KEY");
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required runtime value: " + "STRIPE_SECRET_KEY");
        }
        return value.trim();
    }

    private static String optional(String key, String fallback) {
        String value = readRuntimeValue(key);
        return (value == null || value.isBlank()) ? fallback : value.trim();
    }

    private static String readRuntimeValue(String key) {
        String fromSystemProperty = System.getProperty(key);
        if (fromSystemProperty != null && !fromSystemProperty.isBlank()) {
            return fromSystemProperty.trim();
        }
        return System.getenv(key);
    }

    private static long parseLong(String raw) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ex) {
            return 1200L;
        }
    }
}
