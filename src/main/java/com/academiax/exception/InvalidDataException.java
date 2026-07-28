package com.academiax.exception;

public class InvalidDataException extends RuntimeException {
    
    public InvalidDataException(String message) {
        super(message);
    }
    
    public InvalidDataException(String fieldName, Object fieldValue, String reason) {
        super(String.format("Invalid data for %s : '%s'. Reason: %s", fieldName, fieldValue, reason));
    }
}
