package com.example.capacity.domain.exceptions;

public class CapacityAlreadyExistsException extends RuntimeException {
    public CapacityAlreadyExistsException(String message) {
        super(message);
    }
}
