package com.ylqi007.pizzaorderingservice.module;

import com.google.inject.AbstractModule;
import com.ylqi007.pizzaorderingservice.BillingService;
import com.ylqi007.pizzaorderingservice.RealBillingService;
import com.ylqi007.pizzaorderingservice.processors.CreditCardProcessor;
import com.ylqi007.pizzaorderingservice.processors.SquareCreditCardProcessor;
import com.ylqi007.pizzaorderingservice.processors.TransactionLog;

public class BillingServiceModule extends AbstractModule {

    @Override
    protected void configure() {
        // bind(TransactionLog.class).to(TransactionLog.class);
        bind(CreditCardProcessor.class).to(SquareCreditCardProcessor.class);
        bind(BillingService.class).to(RealBillingService.class);
    }
}
