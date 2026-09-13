package com.netpoint.ticketing.Exceptions;

public class PaymentException extends RuntimeException {
    public PaymentException(String message) {
        super(message);
    }
}