package com.edusphere.exceptions;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Integer userId) {
        super("Id-ul " + userId + " al user-ului este invalid");
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}
