package com.ylqi007.pizzaorderingservice.processors;

import com.ylqi007.pizzaorderingservice.exceptions.UnreachableException;
import com.ylqi007.pizzaorderingservice.types.ChargeResult;

public class TransactionLog {

    public void logChargebackResult(ChargeResult chargeResult) {
        System.out.println(chargeResult);
    }


    public void logConnectException(UnreachableException e) {

    }

    public void logChargeResult(ChargeResult result) {

    }
}
