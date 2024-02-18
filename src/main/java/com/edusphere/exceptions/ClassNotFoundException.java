package com.edusphere.exceptions;

public class ClassNotFoundException extends RuntimeException {
    public ClassNotFoundException(Integer classId) {
        super("Clasa cu id-ul " + classId + " nu exista in organizatia curenta.");
    }
}
