package com.edusphere.exceptions.invoices;

public class InvoiceAlreadyPaidException extends RuntimeException {

    public InvoiceAlreadyPaidException(Integer invoiceId) {
        super("Factura cu id-ul " + invoiceId + " este deja platita." );
    }
}
