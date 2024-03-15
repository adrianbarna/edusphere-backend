package com.edusphere.exceptions.payments;

public class PaymentAlreadyPaidException extends RuntimeException {

    public PaymentAlreadyPaidException(Integer paymentId) {
        super("Factura cu id-ul " + paymentId + " este deja platita." );
    }
}
