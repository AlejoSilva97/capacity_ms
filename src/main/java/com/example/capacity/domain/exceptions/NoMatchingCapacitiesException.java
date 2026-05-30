package com.example.capacity.domain.exceptions;

public class NoMatchingCapacitiesException extends RuntimeException{
    public NoMatchingCapacitiesException(String message){
        super(message);
    }
}
