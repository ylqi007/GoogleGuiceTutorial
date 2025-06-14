package com.ylqi007.pizzaorderingservice.exceptions;

public class UnreachableException extends Exception {
    public UnreachableException(String message) {
        super(message);
    }

    public UnreachableException(String message, Throwable cause) {
        super(message, cause);
    }
}
