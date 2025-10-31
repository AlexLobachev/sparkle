package com.example.sparkle.sparkle.exception;

public class NotFound extends RuntimeException{

    public NotFound(String message) {
        super(message);


    }
    public String getErrorMessage(){
        return getMessage();
    }
}
