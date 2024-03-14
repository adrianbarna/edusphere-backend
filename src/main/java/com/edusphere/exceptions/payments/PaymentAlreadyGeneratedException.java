package com.edusphere.exceptions.payments;

import java.time.LocalDate;

public class PaymentAlreadyGeneratedException extends RuntimeException {

    public PaymentAlreadyGeneratedException() {
        super("Plata a fost deja generata pentru luna: " + LocalDate.now().getMonth());
    }
}
