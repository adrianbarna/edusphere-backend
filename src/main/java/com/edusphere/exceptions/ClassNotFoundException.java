package com.edusphere.exceptions;

public class ClassNotFoundException extends RuntimeException {
    public ClassNotFoundException(Integer classId) {
        super("Nu exista clasa cu id-ul " + classId);
    }
}
