package com.example.sparkle.sparkle.exception;

public class BadRequest extends RuntimeException {

    public BadRequest(String message) {
        super(message);


    }
    public String getErrorMessage(){
        return getMessage();
    }
}
