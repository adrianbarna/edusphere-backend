package com.edusphere.services;


import com.edusphere.entities.ChildEntity;
import com.edusphere.entities.DaysNotChargedEntity;
import com.edusphere.entities.PaymentEntity;
import com.edusphere.entities.SkippedDaysEntity;
import com.edusphere.exceptions.ChildNotFoundException;
import com.edusphere.exceptions.payments.PaymentAlreadyGeneratedException;
import com.edusphere.exceptions.payments.PaymentAlreadyPaidException;
import com.edusphere.exceptions.payments.PaymentNotFoundException;
import com.edusphere.mappers.PaymentMapper;
import com.edusphere.repositories.ChildRepository;
import com.edusphere.repositories.DaysOffRepository;
import com.edusphere.repositories.PaymentRepository;
import com.edusphere.repositories.SkippedDaysRepository;
import com.edusphere.vos.PaymentVO;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Service
// just a demo on how to generate an invoice
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SkippedDaysRepository skippedDaysRepository;
    private final PaymentMapper paymentMapper;
    private final ChildRepository childRepository;
    private final DaysOffRepository daysOffRepository;

    public PaymentService(PaymentRepository paymentRepository, SkippedDaysRepository skippedDaysRepository,
                          PaymentMapper paymentMapper, ChildRepository childRepository,
                          DaysOffRepository daysOffRepository) {
        this.paymentRepository = paymentRepository;
        this.skippedDaysRepository = skippedDaysRepository;
        this.paymentMapper = paymentMapper;
        this.childRepository = childRepository;
        this.daysOffRepository = daysOffRepository;
    }

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

    @Transactional
    //TODO add tests for this
    //TODO this should be called from a batch for each child on the last day of the month
    public PaymentVO generatePaymentForMonth(Integer childId, Integer organizationId) {

        throwExceptionIfPaymentIsAlreadyGenerated();

        ChildEntity childEntity = childRepository.findByIdAndParentOrganizationId(childId, organizationId)
                .orElseThrow(() -> new ChildNotFoundException(childId));

        int paymentAmount = getInitialAmount(childId, organizationId);
        paymentAmount += childEntity.getBaseTax();

        List<SkippedDaysEntity> unproccessedSkippedDays = skippedDaysRepository.findUnproccessedByChildId(childId);
        int skippedDays = getTheNumberOfSkippedDays(unproccessedSkippedDays);
        int totalWeekdaysForCurrentMonth = getTotalWeekdaysForCurrentMonth();
        int daysOff= getDaysNotCharged(organizationId);
        int daysChildCheckedIn = totalWeekdaysForCurrentMonth - skippedDays - daysOff;

        paymentAmount += daysChildCheckedIn * childEntity.getMealPrice();

        boolean isPaid = false;
        isPaid = ifAmountIsNegativeSetPaymentAsPaid(paymentAmount, isPaid);

        PaymentEntity savedPaymentEntity = getSavedPaymentEntity(childEntity, paymentAmount, isPaid);

        markSkippedDaysAsProcessed(unproccessedSkippedDays);
        return paymentMapper.toVO(savedPaymentEntity);
    }

    private int getDaysNotCharged(Integer organizationId) {
            List<DaysNotChargedEntity> daysOffEntities = daysOffRepository.findByOrganizationId(organizationId);

            Calendar cal = Calendar.getInstance();
            int currentMonth = cal.get(Calendar.MONTH);
            int currentYear = cal.get(Calendar.YEAR);

            long count = daysOffEntities.stream()
                    .filter(daysOff -> {
                        cal.setTime(daysOff.getDate());
                        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                        int month = cal.get(Calendar.MONTH);
                        int year = cal.get(Calendar.YEAR);

                        // Check if the day is in the current month and year, and not on a weekend
                        return month == currentMonth && year == currentYear &&
                                (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY);
                    })
                    .count();

            return (int) count;
        }

    private static void markSkippedDaysAsProcessed(List<SkippedDaysEntity> unproccessedSkippedDays) {
        unproccessedSkippedDays
                .forEach(skippedDaysEntity -> skippedDaysEntity.setProccessed(true));
    }

    private PaymentEntity getSavedPaymentEntity(ChildEntity childEntity, int paymentAmount, boolean isPaid) {
        PaymentEntity paymentEntity = PaymentEntity.builder()
                .child(childEntity)
                .amount(paymentAmount)
                .dueDate(new Date())
                .issueDate(new Date())
                .isPaid(isPaid)
                .build();
        return paymentRepository.save(paymentEntity);
    }

    private static boolean ifAmountIsNegativeSetPaymentAsPaid(int paymentAmount, boolean isPaid) {
        if (paymentAmount <= 0) {
            isPaid = true;
        }
        return isPaid;
    }

    private int getTheNumberOfSkippedDays(List<SkippedDaysEntity> unproccessedSkippedDays) {
        return unproccessedSkippedDays.stream()
                // Assuming a method to convert SkippedDaysEntity to SkippedDaysVO exists
                .mapToInt(this::getWeekdayCountForSkippedDays)
                .sum();
    }

    private int getInitialAmount(Integer childId, Integer organizationId) {
        Optional<PaymentEntity> lastPaymentByChildIdAndOrganizationId = paymentRepository.findLastPaymentByChildIdAndOrganizationId(childId, organizationId);
        int paymentAmount = 0;

        if (lastPaymentByChildIdAndOrganizationId.isPresent()) {
            if (lastPaymentByChildIdAndOrganizationId.get().getAmount() < 0) {
                paymentAmount = lastPaymentByChildIdAndOrganizationId.get().getAmount();
            }
        }
        return paymentAmount;
    }

    private void throwExceptionIfPaymentIsAlreadyGenerated() {
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        if (paymentRepository.existsPaymentsForCurrentMonth(currentYear, currentMonth)) {
            throw new PaymentAlreadyGeneratedException();
        }
    }


    public PaymentVO markPaymentAsPaidOrUnpaid(Integer paymentId, boolean isPaid, Integer organizationId) {
        PaymentEntity paymentEntity = paymentRepository.findByIdAndChildParentOrganizationId(paymentId, organizationId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        if (isPaid && paymentEntity.isPaid()) {
            throw new PaymentAlreadyPaidException(paymentId);
        }

        paymentEntity.setPaid(isPaid);
        paymentRepository.save(paymentEntity);
        return paymentMapper.toVO(paymentEntity);
    }

    public static int getTotalWeekdaysForCurrentMonth() {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = YearMonth.from(startOfMonth).atEndOfMonth();

        return getWeekdaysCount(startOfMonth, endOfMonth);
    }

    private static int getWeekdaysCount(LocalDate startOfMonth, LocalDate endOfMonth) {
        int weekdays = 0;
        LocalDate date = startOfMonth;
        while (!date.isAfter(endOfMonth)) {
            // Check if the day is a weekday (Monday to Friday)
            if (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                weekdays++;
            }
            date = date.plusDays(1);
        }

        return weekdays;
    }

    private int getWeekdayCountForSkippedDays(SkippedDaysEntity skippedDaysEntity) {
        LocalDate startDate = skippedDaysEntity.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = skippedDaysEntity.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        return getWeekdaysCount(startDate, endDate);
    }
}
