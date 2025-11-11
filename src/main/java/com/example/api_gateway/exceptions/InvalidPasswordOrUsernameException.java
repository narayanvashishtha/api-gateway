package com.example.api_gateway.exceptions;

public class InvalidPasswordOrUsernameException extends RuntimeException{
    public InvalidPasswordOrUsernameException(String message) {
        super(message);
    }
}
