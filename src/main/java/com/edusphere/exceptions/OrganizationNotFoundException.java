package com.edusphere.exceptions;

public class OrganizationNotFoundException extends RuntimeException {

    public OrganizationNotFoundException(Integer organizationId) {
        super("Id-ul organizatiei este invalid: " + organizationId);
    }
}
