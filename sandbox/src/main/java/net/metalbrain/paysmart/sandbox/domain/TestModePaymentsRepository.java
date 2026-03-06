package net.metalbrain.paysmart.sandbox.domain;

import com.stripe.exception.StripeException;
import net.metalbrain.paysmart.sandbox.domain.model.ProductPriceSnapshot;

public interface TestModePaymentsRepository {
    ProductPriceSnapshot createRecurringProductPrice(
            String productName,
            String productDescription,
            String currency,
            long unitAmountMinor
    ) throws StripeException;
}
