package com.ylqi007.pizzaorderingservice;

import com.ylqi007.pizzaorderingservice.exceptions.UnreachableException;
import com.ylqi007.pizzaorderingservice.processors.CreditCardProcessor;
import com.ylqi007.pizzaorderingservice.processors.TransactionLog;
import com.ylqi007.pizzaorderingservice.types.ChargeResult;
import com.ylqi007.pizzaorderingservice.types.CreditCard;
import com.ylqi007.pizzaorderingservice.types.PizzaOrder;
import com.ylqi007.pizzaorderingservice.types.Receipt;

/**
 * Direct constructor calls
 * This code poses problems for modularity and testability. 
 * The direct, compile-time dependency on the real credit card processor means that testing the code will charge a credit card! 
 * It's also awkward to test what happens when the charge is declined or when the service is unavailable.
 * 
 * 直接通过 new 关键字创建 CreditCardProcessor and TransactionLog，这种强耦合会对后续的 unit test 造成困难。
 */
public class RealBillingService implements BillingService {
    @Override
    public Receipt chargeOrder(PizzaOrder order, CreditCard creditCard) {
        CreditCardProcessor processor = new CreditCardProcessor();
        TransactionLog transactionLog = new TransactionLog();

        try {
            ChargeResult result = processor.charge(creditCard, order.getAmount());
            transactionLog.logChargebackResult(result);

            return result.wasSuccessful()
                    ? Receipt.forSuccessfulCharge(order.getAmount())
                    : Receipt.forDeclinedCharge(result.getDeclineMessage());
        } catch (UnreachableException e) {
            transactionLog.logConnectException(e);
            return Receipt.forSystemFailure(e.getMessage());
        }
    }
}
