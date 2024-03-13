package com.edusphere.exceptions.payments;

public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(Integer paymentId) {
        super("Factura cu id-ul " + paymentId + " este invalida." );
    }
}
