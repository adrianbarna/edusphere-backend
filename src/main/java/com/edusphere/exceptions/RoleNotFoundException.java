package com.edusphere.exceptions;

public class RoleNotFoundException extends RuntimeException {

    public RoleNotFoundException(Integer roleId) {
        super("Rolul cu id-ul " + roleId + " este invalid pentru organizatia curenta" );
    }

    public RoleNotFoundException(String message) {
        super(message);
    }
}
