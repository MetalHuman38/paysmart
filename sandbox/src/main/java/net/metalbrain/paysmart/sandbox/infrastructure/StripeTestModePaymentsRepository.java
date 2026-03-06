package net.metalbrain.paysmart.sandbox.infrastructure;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.ProductCreateParams;
import net.metalbrain.paysmart.sandbox.domain.TestModePaymentsRepository;
import net.metalbrain.paysmart.sandbox.domain.model.ProductPriceSnapshot;

public class StripeTestModePaymentsRepository implements TestModePaymentsRepository {

    public StripeTestModePaymentsRepository(String stripeSecretKey) {
        Stripe.apiKey = stripeSecretKey;
    }

    @Override
    public ProductPriceSnapshot createRecurringProductPrice(
            String productName,
            String productDescription,
            String currency,
            long unitAmountMinor
    ) throws StripeException {
        ProductCreateParams productParams = ProductCreateParams.builder()
                .setName(productName)
                .setDescription(productDescription)
                .build();
        Product product = Product.create(productParams);

        PriceCreateParams priceParams = PriceCreateParams.builder()
                .setProduct(product.getId())
                .setCurrency(currency)
                .setUnitAmount(unitAmountMinor)
                .setRecurring(
                        PriceCreateParams.Recurring.builder()
                                .setInterval(PriceCreateParams.Recurring.Interval.MONTH)
                                .build()
                )
                .build();
        Price price = Price.create(priceParams);

        return new ProductPriceSnapshot(
                product.getId(),
                price.getId(),
                currency,
                unitAmountMinor
        );
    }
}
