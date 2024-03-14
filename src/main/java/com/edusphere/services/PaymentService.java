package com.edusphere.services;


import com.edusphere.entities.ChildEntity;
import com.edusphere.entities.PaymentEntity;
import com.edusphere.exceptions.ChildNotFoundException;
import com.edusphere.exceptions.payments.PaymentAlreadyPaidException;
import com.edusphere.exceptions.payments.PaymentNotFoundException;
import com.edusphere.mappers.PaymentMapper;
import com.edusphere.mappers.SkippedDaysMapper;
import com.edusphere.repositories.ChildRepository;
import com.edusphere.repositories.PaymentRepository;
import com.edusphere.repositories.SkippedDaysRepository;
import com.edusphere.vos.PaymentVO;
import com.edusphere.vos.SkippedDaysVO;
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
import java.util.List;


@Service
// just a demo on how to generate an invoice
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SkippedDaysRepository skippedDaysRepository;
    private final PaymentMapper paymentMapper;
    private final ChildRepository childRepository;
    private final SkippedDaysMapper skippedDaysMapper;

    public PaymentService(PaymentRepository paymentRepository, SkippedDaysRepository skippedDaysRepository,
                          PaymentMapper paymentMapper, ChildRepository childRepository, SkippedDaysMapper skippedDaysMapper) {
        this.paymentRepository = paymentRepository;
        this.skippedDaysRepository = skippedDaysRepository;
        this.paymentMapper = paymentMapper;
        this.childRepository = childRepository;
        this.skippedDaysMapper = skippedDaysMapper;
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
    //TODO add tests for skipDaysPeriods
    public List<PaymentVO> getChildPaymentsByMonth(Integer childId, YearMonth month, Integer organizationId) {
        ChildEntity childEntity = getChildEntity(childId, organizationId);

        List<SkippedDaysVO> unprocessedSkipDays = getSkippedDaysPeriods(childId);
        List<PaymentVO> paymentsForChildForMonth = getPaymentsForChildForMonth(childId, month, organizationId);


        return substractSkippedDaysAmountsFromPaymentsAmountForChildPaymentList(paymentsForChildForMonth, unprocessedSkipDays, childEntity);
    }

    //TODO add skipDaysPeriods logic
    public List<PaymentVO> getParentPaymentsByMonth(Integer parentId, YearMonth month, Integer organizationId) {
        return getPaymentsForParentId(parentId, month, organizationId);
    }

    public PaymentVO markPaymentAsPaidOrUnpaid(Integer paymentId, boolean isPaid, Integer organizationId) {
        PaymentEntity paymentEntity = paymentRepository.findByIdAndChildParentOrganizationId(paymentId, organizationId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        if (isPaid && paymentEntity.getIsPaid()) {
            throw new PaymentAlreadyPaidException(paymentId);
        }

        paymentEntity.setIsPaid(isPaid);
        paymentRepository.save(paymentEntity);
        return paymentMapper.toVO(paymentEntity);
    }

    private List<PaymentVO> getPaymentsForParentId(Integer parentId, YearMonth month, Integer organizationId) {
        return paymentRepository.findByParentIdAndMonthAndYearAndOrganizationId(parentId, month.getMonthValue(),
                        month.getYear(), organizationId)
                .stream()
                .map(paymentMapper::toVO)
                .toList();
    }

    private int getAmountForSkippedDaysEntity(Integer childMealPrice, SkippedDaysVO skippedDaysVO) {
        int numberOfWeekdays = getWeekdayCountForSkippedDays(skippedDaysVO);

        return childMealPrice * numberOfWeekdays;
    }

    private int getWeekdayCountForSkippedDays(SkippedDaysVO skippedDaysEntity) {
        LocalDate startDate = skippedDaysEntity.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = skippedDaysEntity.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        return getWeekdayCount(startDate, endDate);
    }

    private int getWeekdayCount(LocalDate startDate, LocalDate endDate) {
        int weekdayCount = 0;
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            // Check if the current date is a weekday (Monday to Friday)
            if (currentDate.getDayOfWeek() != DayOfWeek.SATURDAY && currentDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                weekdayCount++;
            }
            // Move to the next day
            currentDate = currentDate.plusDays(1);
        }

        return weekdayCount;
    }

    private static void substractSkipDaysAmountFromPaymentsAmountAndAddSetItAsProccessedInMemory(PaymentVO paymentVO,
                                                                                                 SkippedDaysVO skippedDaysVO, int amountForSkippedDays) {
        paymentVO.setAmountWithSkipDays(paymentVO.getAmountWithoutSkipDays() - amountForSkippedDays);
        paymentVO.addSkippedDaysVO(skippedDaysVO);
        skippedDaysVO.setProccessed(true);
        skippedDaysVO.setAmount(amountForSkippedDays);
    }

    private static boolean skipDaysAmountCanBeSubstractedFromPaymentAmount(PaymentVO paymentVO, int amountForSkippedDays) {
        return amountForSkippedDays < paymentVO.getAmountWithSkipDays() && paymentVO.getAmountWithSkipDays() - amountForSkippedDays >= 0;
    }

    private static List<SkippedDaysVO> getUnproccessedSkipDaysFromInMemorySkipDays(List<SkippedDaysVO> unprocessedSkipDays) {
        return unprocessedSkipDays.stream()
                .filter(skippedDaysEntity -> !skippedDaysEntity.getProccessed())
                .toList();
    }

    private List<PaymentVO> getPaymentsForChildForMonth(Integer childId, YearMonth month, Integer organizationId) {
        return paymentRepository.findByChildIdAndMonthAndYearAndOrganizationId(childId, month.getMonthValue(),
                        month.getYear(), organizationId)
                .stream()
                .map(paymentMapper::toVO)
                .toList();
    }

    private ChildEntity getChildEntity(Integer childId, Integer organizationId) {
        return childRepository.findByIdAndParentOrganizationId(childId, organizationId)
                .orElseThrow(() -> new ChildNotFoundException(childId));
    }

    private List<SkippedDaysVO> getSkippedDaysPeriods(Integer childId) {
        return skippedDaysRepository.findUnproccessedByChildId(childId)
                .stream()
                .map(skippedDaysMapper::toVO)
                .toList();
    }

    private List<PaymentVO> substractSkippedDaysAmountsFromPaymentsAmountForChildPaymentList(List<PaymentVO> paymentsForChildForMonth,
                                                                                             List<SkippedDaysVO> unprocessedSkipDays,
                                                                                             ChildEntity childEntity) {
        paymentsForChildForMonth.forEach(paymentVO -> substractSkippedDaysAmountsFromPaymentsAmount(paymentVO,
                unprocessedSkipDays, childEntity.getMealPrice()));

        return paymentsForChildForMonth;
    }

    private void substractSkippedDaysAmountsFromPaymentsAmount(PaymentVO paymentVO, List<SkippedDaysVO> unprocessedSkipDays, Integer childMealPrice) {
        List<SkippedDaysVO> unprocessedBeforeProcessingPayment = getUnproccessedSkipDaysFromInMemorySkipDays(unprocessedSkipDays);

        unprocessedBeforeProcessingPayment.forEach(skippedDaysVO -> {
            int amountForCurrentSkipDaysPeriod = getAmountForSkippedDaysEntity(childMealPrice, skippedDaysVO);

            if (skipDaysAmountCanBeSubstractedFromPaymentAmount(paymentVO, amountForCurrentSkipDaysPeriod)) {
                substractSkipDaysAmountFromPaymentsAmountAndAddSetItAsProccessedInMemory(paymentVO, skippedDaysVO, amountForCurrentSkipDaysPeriod);
            }
        });
    }
}
