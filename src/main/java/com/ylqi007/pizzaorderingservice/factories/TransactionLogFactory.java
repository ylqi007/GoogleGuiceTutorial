package com.ylqi007.pizzaorderingservice.factories;

import com.ylqi007.pizzaorderingservice.processors.TransactionLog;


public class TransactionLogFactory {

    @lombok.Setter(lombok.AccessLevel.PRIVATE)
    private static TransactionLog transactionLog;

    public static TransactionLog getTransactionLog() {
        if(transactionLog == null) {
            return new TransactionLog();
        }
        return transactionLog;
    }
}
