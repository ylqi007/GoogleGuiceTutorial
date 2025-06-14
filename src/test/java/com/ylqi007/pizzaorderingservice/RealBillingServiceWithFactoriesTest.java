package com.ylqi007.pizzaorderingservice;


import com.ylqi007.pizzaorderingservice.processors.CreditCardProcessor;
import com.ylqi007.pizzaorderingservice.processors.TransactionLog;
import com.ylqi007.pizzaorderingservice.types.CreditCard;
import com.ylqi007.pizzaorderingservice.types.PizzaOrder;

class RealBillingServiceWithFactoriesTest {
    private static final PizzaOrder pizzaOrder = PizzaOrder.builder().amount(1000).build();
    private static final CreditCard creditCard = CreditCard.builder().creditCardNumber("1234").build();
    private static final TransactionLog transactionLog = new TransactionLog();
    private static final CreditCardProcessor creditCardProcessor = new CreditCardProcessor();

}