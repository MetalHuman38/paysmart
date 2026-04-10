package net.metalbrain.paysmart.sandbox.app;

import com.stripe.exception.StripeException;
import net.metalbrain.paysmart.sandbox.di.SandboxContainer;

public final class SandboxServerMain {

    public static void main(String[] args) {
        try {
            SandboxContainer container = SandboxContainer.boot();
            var config = container.config();
            var result = container.createInvestorDemoPlan().execute(config);

            System.out.println("Sandbox product created successfully");
            System.out.println("productId=" + result.productId());
            System.out.println("priceId=" + result.priceId());
            System.out.println("currency=" + result.currency());
            System.out.println("unitAmountMinor=" + result.unitAmountMinor());
        } catch (IllegalStateException | IllegalArgumentException ex) {
            System.err.println("Configuration error: " + ex.getMessage());
            System.exit(2);
        } catch (StripeException ex) {
            System.err.println("Stripe API error: " + ex.getMessage());
            System.exit(3);
        }
    }
}
