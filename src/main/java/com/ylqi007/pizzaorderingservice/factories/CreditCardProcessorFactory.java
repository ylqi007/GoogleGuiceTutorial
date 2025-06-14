package com.ylqi007.pizzaorderingservice.factories;

import com.ylqi007.pizzaorderingservice.processors.CreditCardProcessor;
import com.ylqi007.pizzaorderingservice.processors.SquareCreditCardProcessor;

/**
 * A factory class decouples the client and implementing class. A simple factory uses static methods to get and set mock implementations for interfaces.
 * A factory is implemented with some boilerplate code:
 */
public class CreditCardProcessorFactory {

    private static CreditCardProcessor instance;

    public static void setInstance(CreditCardProcessor processor) {
        instance = processor;
    }

    public static CreditCardProcessor getInstance() {
        if (instance == null) {
            return new SquareCreditCardProcessor();
        }

        return instance;
    }
}