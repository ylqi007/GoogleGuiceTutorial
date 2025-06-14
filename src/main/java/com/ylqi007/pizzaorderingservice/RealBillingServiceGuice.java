package com.ylqi007.pizzaorderingservice;

import com.google.inject.Inject;
import com.ylqi007.pizzaorderingservice.exceptions.UnreachableException;
import com.ylqi007.pizzaorderingservice.processors.CreditCardProcessor;
import com.ylqi007.pizzaorderingservice.processors.TransactionLog;
import com.ylqi007.pizzaorderingservice.types.ChargeResult;
import com.ylqi007.pizzaorderingservice.types.CreditCard;
import com.ylqi007.pizzaorderingservice.types.PizzaOrder;
import com.ylqi007.pizzaorderingservice.types.Receipt;

import java.lang.annotation.Inherited;

public class RealBillingServiceGuice implements BillingService {
    private final CreditCardProcessor creditCardProcessor;
    private final TransactionLog transactionLog;

    @Inject
    public RealBillingServiceGuice(CreditCardProcessor creditCardProcessor, TransactionLog transactionLog) {
        this.creditCardProcessor = creditCardProcessor;
        this.transactionLog = transactionLog;
    }

    public Receipt chargeOrder(PizzaOrder order, CreditCard creditCard) {
        try {
            ChargeResult result = creditCardProcessor.charge(creditCard, order.getAmount());
            transactionLog.logChargeResult(result);

            return result.wasSuccessful()
                    ? Receipt.forSuccessfulCharge(order.getAmount())
                    : Receipt.forDeclinedCharge(result.getDeclineMessage());
        } catch (UnreachableException e) {
            transactionLog.logConnectException(e);
            return Receipt.forSystemFailure(e.getMessage());
        }
    }
}
