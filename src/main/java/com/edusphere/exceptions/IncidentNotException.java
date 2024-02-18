package com.edusphere.exceptions;

public class IncidentNotException extends RuntimeException {

    public IncidentNotException(Integer userId) {
        super("Id-ul incidentului " + userId + "este invalid");
    }

    public IncidentNotException(String message){
        super(message);
    }
}
