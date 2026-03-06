package net.metalbrain.paysmart.sandbox.domain.model;

public record ProductPriceSnapshot(
        String productId,
        String priceId,
        String currency,
        long unitAmountMinor
) {
}
