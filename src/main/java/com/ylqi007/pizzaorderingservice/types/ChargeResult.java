package com.ylqi007.pizzaorderingservice.types;

import lombok.Getter;

@Getter
public class ChargeResult {
    boolean success;
    String declineMessage;

    public boolean wasSuccessful() {
        return success;
    }
}
