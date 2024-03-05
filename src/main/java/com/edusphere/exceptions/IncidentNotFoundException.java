package com.edusphere.exceptions;

public class IncidentNotFoundException extends RuntimeException {

    public IncidentNotFoundException(Integer incidentId) {
        super("Id-ul incidentului " + incidentId + " este invalid");
    }

    public IncidentNotFoundException(String message){
        super(message);
    }
}
