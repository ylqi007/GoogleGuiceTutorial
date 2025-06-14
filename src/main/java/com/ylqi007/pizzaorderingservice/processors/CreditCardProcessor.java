package com.ylqi007.pizzaorderingservice.processors;

import com.ylqi007.pizzaorderingservice.exceptions.UnreachableException;
import com.ylqi007.pizzaorderingservice.types.ChargeResult;
import com.ylqi007.pizzaorderingservice.types.CreditCard;


import java.util.Random;

public class CreditCardProcessor {

    private static Random random = new Random();

    public ChargeResult charge(CreditCard creditCard, int amount) throws UnreachableException {
        if(random.nextBoolean()) {
            System.out.println("CreditCardProcessor::charge called");
            return new ChargeResult();
        }
        throw new UnreachableException("UnreachableException");
    }
}
