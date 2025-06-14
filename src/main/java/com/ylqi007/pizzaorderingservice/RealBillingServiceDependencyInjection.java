package com.ylqi007.pizzaorderingservice;

import com.ylqi007.pizzaorderingservice.exceptions.UnreachableException;
import com.ylqi007.pizzaorderingservice.processors.CreditCardProcessor;
import com.ylqi007.pizzaorderingservice.processors.TransactionLog;
import com.ylqi007.pizzaorderingservice.types.ChargeResult;
import com.ylqi007.pizzaorderingservice.types.CreditCard;
import com.ylqi007.pizzaorderingservice.types.PizzaOrder;
import com.ylqi007.pizzaorderingservice.types.Receipt;

/**
 * Now, whenever we add or remove dependencies, the compiler will remind us what tests need to be fixed.
 * The dependency is exposed in the API signature.
 */
public class RealBillingServiceDependencyInjection implements BillingService {
    private final CreditCardProcessor processor;
    private final TransactionLog transactionLog;

    /**
     * 在构造器中并不 new 对象。对象在别处生成，此处直接构建 reference
     */
    public RealBillingServiceDependencyInjection(CreditCardProcessor processor,
                              TransactionLog transactionLog) {
        this.processor = processor;
        this.transactionLog = transactionLog;
    }

    public Receipt chargeOrder(PizzaOrder order, CreditCard creditCard) {
        try {
            ChargeResult result = processor.charge(creditCard, order.getAmount());
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
