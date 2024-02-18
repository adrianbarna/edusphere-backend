package com.edusphere.exceptions;

public class ParentNotFoundException extends RuntimeException {

    public ParentNotFoundException(Integer parentId) {
        super("Id-ul parintelui este invalid: " + parentId);
    }


}
