package com.ylqi007.pizzaorderingservice.types;

import lombok.Builder;

@Builder
public class Receipt {
    private String receiptId;
    private String message;
    private int amount;

    public static Receipt forSuccessfulCharge(int amount) {
        return Receipt.builder()
                .amount(amount)
                .message("Charge successful")
                .build();
    }

    public static Receipt forDeclinedCharge(String message) {
        return Receipt.builder()
                .message(message)
                .build();
    }

    public static Receipt forSystemFailure(String message) {
        return Receipt.builder()
                .message("System Failure")
                .build();
    }
}
