package com.edusphere.exceptions;

public class RoleAlreadyExistsException extends RuntimeException {
    public RoleAlreadyExistsException(String name) {
        super("Rolul: " + name +" deja exista");
    }
}
