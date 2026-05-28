package com.example.capacity.domain.exceptions;

public class TechnologyNotFoundException extends RuntimeException {
    public TechnologyNotFoundException(String message) {
        super(message);
    }
}
