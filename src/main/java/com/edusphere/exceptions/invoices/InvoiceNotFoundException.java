package com.edusphere.exceptions.invoices;

public class InvoiceNotFoundException extends RuntimeException {

    public InvoiceNotFoundException(Integer invoiceId) {
        super("Factura cu id-ul " + invoiceId + " este invalida." );
    }
}
