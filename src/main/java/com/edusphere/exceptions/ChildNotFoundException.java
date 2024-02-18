package com.edusphere.exceptions;

public class ChildNotFoundException extends RuntimeException {

    public ChildNotFoundException(Integer childId) {
        super("Id-ul copilului: " + childId + " este invalid");
    }

    public ChildNotFoundException(String message) {
        super(message);
    }
}
