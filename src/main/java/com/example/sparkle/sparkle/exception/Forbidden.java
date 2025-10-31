package com.example.sparkle.sparkle.exception;

public class Forbidden extends RuntimeException {



    public Forbidden(String message) {
        super(message);

    }

    public String getErrorMessage() {
        return getMessage();
    }
}
