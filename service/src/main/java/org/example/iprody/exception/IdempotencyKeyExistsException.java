package org.example.iprody.exception;

public class IdempotencyKeyExistsException extends RuntimeException {
    public IdempotencyKeyExistsException(String message) {
        super(message);
    }
}
