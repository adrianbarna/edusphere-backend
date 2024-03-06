package com.edusphere.services;



import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
// just a demo on how to generate an invoice
public class InvoiceService {

    private ByteArrayInputStream generateInvoice() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Add header
        Paragraph header = new Paragraph("INVOICE")
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
                .setFontSize(20);
        document.add(header);

        // Add invoice info
        Paragraph invoiceInfo = new Paragraph("Invoice #1024")
                .setTextAlignment(TextAlignment.RIGHT);
        document.add(invoiceInfo);

        // Add billing info
        Paragraph billingTo = new Paragraph("BILLED TO:  Really Great Company")
                .setBold();
        document.add(billingTo);

        Paragraph payTo = new Paragraph("PAY TO:  Avery Davis  123 Anywhere St., Any City  123-456-7890")
                .setBold();
        document.add(payTo);

        // Add bank info
        Paragraph bankInfo = new Paragraph("Bank Really Great Bank\nAccount Name\nJohn Smith\nBSB\n000-000\nAccount Number\n0000 0000");
        document.add(bankInfo);

        // Add table for itemized charges
        float[] columnWidths = {5, 1, 1, 1}; // Set the column widths
        Table table = new Table(UnitValue.createPercentArray(columnWidths));

        // Add headers
        table.addHeaderCell("DESCRIPTION");
        table.addHeaderCell("RATE");
        table.addHeaderCell("HOURS");
        table.addHeaderCell("AMOUNT");

        // Add rows with data
        table.addCell("Content Plan");
        table.addCell("$50/hr");
        table.addCell("4");
        table.addCell("$200.00");

        // ... Add other rows for different items ...

        // Add subtotal, discount and total
        table.addCell(new Paragraph("Sub-Total"));
        table.addCell("");
        table.addCell("");
        table.addCell("$1,250.00");

        table.addCell(new Paragraph("Package Discount (30%)"));
        table.addCell("");
        table.addCell("");
        table.addCell("$375.00");

        table.addCell(new Paragraph("TOTAL"));
        table.addCell("");
        table.addCell("");
        table.addCell("$875.00");

        document.add(table);

        // Add footer
        Paragraph footer = new Paragraph("Payment is required within 14 business days of invoice date. Please send remittance to hello@reallygreatsite.com.\n\nThank you for your business.")
                .setTextAlignment(TextAlignment.CENTER);
        document.add(footer);

        document.close();

        return new ByteArrayInputStream(out.toByteArray());
    }

    public void saveInvoiceToFile(String filePath) {
        ByteArrayInputStream inputStream = generateInvoice();

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buf)) > 0) {
                fos.write(buf, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
