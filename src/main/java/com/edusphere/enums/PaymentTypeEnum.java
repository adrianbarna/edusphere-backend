package com.edusphere.enums;

import lombok.Getter;

@Getter
public enum PaymentTypeEnum {
    TRANSFER("TRANSFER");
    private final String name;

    PaymentTypeEnum(String name) {
        this.name = name;
    }

}
