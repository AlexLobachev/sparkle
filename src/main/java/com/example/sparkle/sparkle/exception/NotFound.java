package com.example.sparkle.sparkle.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class NotFound extends RuntimeException{
    private HttpStatus httpStatus;
    public NotFound(String message,HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;

    }
    public Map<String,HttpStatus> getErrorMessage(){
        return Map.of(getMessage(),httpStatus);
    }
}
