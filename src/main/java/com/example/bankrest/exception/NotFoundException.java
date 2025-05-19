package com.example.bankrest.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
}
