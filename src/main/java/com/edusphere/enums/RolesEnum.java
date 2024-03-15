package com.edusphere.enums;

public enum RolesEnum {
    OWNER("OWNER"),
    ADMIN("ADMIN"),
    TEACHER("TEACHER"),
    PARENT("PARENT");
    private final String name;

    RolesEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
