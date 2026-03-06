package net.metalbrain.paysmart.sandbox.application;

import com.stripe.exception.StripeException;
import net.metalbrain.paysmart.sandbox.config.SandboxConfig;
import net.metalbrain.paysmart.sandbox.domain.TestModePaymentsRepository;
import net.metalbrain.paysmart.sandbox.domain.model.ProductPriceSnapshot;

public class CreateInvestorDemoPlan {

    private final TestModePaymentsRepository repository;

    public CreateInvestorDemoPlan(TestModePaymentsRepository repository) {
        this.repository = repository;
    }

    public ProductPriceSnapshot execute(SandboxConfig config) throws StripeException {
        return repository.createRecurringProductPrice(
                config.defaultProductName(),
                config.defaultProductDescription(),
                config.defaultCurrency(),
                config.defaultUnitAmountMinor()
        );
    }
}
