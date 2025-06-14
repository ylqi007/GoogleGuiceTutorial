package com.ylqi007;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.ylqi007.pizzaorderingservice.BillingService;
import com.ylqi007.pizzaorderingservice.module.BillingServiceModule;
import com.ylqi007.pizzaorderingservice.processors.CreditCardProcessor;
import com.ylqi007.pizzaorderingservice.processors.TransactionLog;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");

        Injector injector = Guice.createInjector(new BillingServiceModule());
        BillingService billingService = injector.getInstance(BillingService.class);

        System.out.println(injector.getInstance(BillingService.class));
        System.out.println(injector.getInstance(TransactionLog.class));
        System.out.println(injector.getInstance(CreditCardProcessor.class));
    }
}