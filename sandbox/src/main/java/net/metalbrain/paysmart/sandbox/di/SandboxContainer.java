package net.metalbrain.paysmart.sandbox.di;

import net.metalbrain.paysmart.sandbox.application.CreateInvestorDemoPlan;
import net.metalbrain.paysmart.sandbox.config.SandboxConfig;
import net.metalbrain.paysmart.sandbox.infrastructure.StripeTestModePaymentsRepository;

public final class SandboxContainer {

    private final SandboxConfig config;
    private final CreateInvestorDemoPlan createInvestorDemoPlan;

    private SandboxContainer(SandboxConfig config) {
        this.config = config;
        var repository = new StripeTestModePaymentsRepository(config.stripeSecretKey());
        this.createInvestorDemoPlan = new CreateInvestorDemoPlan(repository);
    }

    public static SandboxContainer boot() {
        return new SandboxContainer(SandboxConfig.fromEnvironment());
    }

    public SandboxConfig config() {
        return config;
    }

    public CreateInvestorDemoPlan createInvestorDemoPlan() {
        return createInvestorDemoPlan;
    }
}
