package com.edusphere.services;


import com.edusphere.entities.ChildEntity;
import com.edusphere.entities.InvoiceEntity;
import com.edusphere.exceptions.ChildNotFoundException;
import com.edusphere.exceptions.invoices.InvoiceAlreadyPaidException;
import com.edusphere.exceptions.invoices.InvoiceNotFoundException;
import com.edusphere.mappers.InvoiceMapper;
import com.edusphere.mappers.SkippedDaysMapper;
import com.edusphere.repositories.ChildRepository;
import com.edusphere.repositories.InvoiceRepository;
import com.edusphere.repositories.SkippedDaysRepository;
import com.edusphere.vos.InvoiceVO;
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
import java.util.stream.Collectors;


@Service
// just a demo on how to generate an invoice
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final SkippedDaysRepository skippedDaysRepository;
    private final InvoiceMapper invoiceMapper;
    private final ChildRepository childRepository;
    private final SkippedDaysMapper skippedDaysMapper;

    public InvoiceService(InvoiceRepository invoiceRepository, SkippedDaysRepository skippedDaysRepository,
                          InvoiceMapper invoiceMapper, ChildRepository childRepository, SkippedDaysMapper skippedDaysMapper) {
        this.invoiceRepository = invoiceRepository;
        this.skippedDaysRepository = skippedDaysRepository;
        this.invoiceMapper = invoiceMapper;
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
    public List<InvoiceVO> getChildInvoicesByMonth(Integer childId, YearMonth month, Integer organizationId) {
        ChildEntity childEntity = getChildEntity(childId, organizationId);

        List<SkippedDaysVO> unprocessedSkipDays = getSkippedDaysPeriods(childId);
        List<InvoiceVO> invoicesForChildForMonth = getInvoicesForChildForMonth(childId, month, organizationId);


        return substractSkippedDaysAmountsFromInvoicesAmountForChildInvoiceList(invoicesForChildForMonth, unprocessedSkipDays, childEntity);
    }

    //TODO add skipDaysPeriods logic
    public List<InvoiceVO> getParentInvoicesByMonth(Integer parentId, YearMonth month, Integer organizationId) {
        return getInvoicesForParentId(parentId, month, organizationId);
    }

    public InvoiceVO markInvoiceAsPaid(Integer invoiceId, Integer organizationId) {
        InvoiceEntity invoiceEntity = invoiceRepository.findByIdAndChildParentOrganizationId(invoiceId, organizationId)
                .orElseThrow(() -> new InvoiceNotFoundException(invoiceId));

        if (invoiceEntity.getIsPaid()) {
            throw new InvoiceAlreadyPaidException(invoiceId);
        }
        invoiceEntity.setIsPaid(true);
        invoiceRepository.save(invoiceEntity);
        return invoiceMapper.toVO(invoiceEntity);
    }

    private List<InvoiceVO> getInvoicesForParentId(Integer parentId, YearMonth month, Integer organizationId) {
        List<InvoiceVO> invoicesVOs = invoiceRepository.findByParentIdAndMonthAndYearAndOrganizationId(parentId, month.getMonthValue(),
                        month.getYear(), organizationId)
                .stream()
                .map(invoiceMapper::toVO)
                .collect(Collectors.toList());
        return invoicesVOs;
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

    private static void substractSkipDaysAmountFromInvoicesAmountAndAddSetItAsProccessedInMemory(InvoiceVO invoiceVO, SkippedDaysVO skippedDaysVO, int amountForSkippedDays) {
        invoiceVO.setAmountWithSkipDays(invoiceVO.getAmountWithoutSkipDays() - amountForSkippedDays);
        invoiceVO.addSkippedDaysVO(skippedDaysVO);
        skippedDaysVO.setProccessed(true);
        skippedDaysVO.setAmount(amountForSkippedDays);
    }

    private static boolean skipDaysAmountCanBeSubstractedFromInvoiceAmount(InvoiceVO invoiceVO, int amountForSkippedDays) {
        return amountForSkippedDays < invoiceVO.getAmountWithSkipDays() && invoiceVO.getAmountWithSkipDays() - amountForSkippedDays >= 0;
    }

    private static List<SkippedDaysVO> getUnproccessedSkipDaysFromInMemorySkipDays(List<SkippedDaysVO> unprocessedSkipDays) {
        return unprocessedSkipDays.stream()
                .filter(skippedDaysEntity -> !skippedDaysEntity.getProccessed())
                .toList();
    }

    private List<InvoiceVO> getInvoicesForChildForMonth(Integer childId, YearMonth month, Integer organizationId) {
        return invoiceRepository.findByChildIdAndMonthAndYearAndOrganizationId(childId, month.getMonthValue(),
                        month.getYear(), organizationId)
                .stream()
                .map(invoiceMapper::toVO)
                .collect(Collectors.toList());
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

    private List<InvoiceVO> substractSkippedDaysAmountsFromInvoicesAmountForChildInvoiceList(List<InvoiceVO> invoicesForChildForMonth, List<SkippedDaysVO> unprocessedSkipDays, ChildEntity childEntity) {
        invoicesForChildForMonth.forEach(invoiceVO -> {
            substractSkippedDaysAmountsFromInvoicesAmount(invoiceVO, unprocessedSkipDays, childEntity.getMealPrice());
        });

        return invoicesForChildForMonth;
    }

    private void substractSkippedDaysAmountsFromInvoicesAmount(InvoiceVO invoiceVO, List<SkippedDaysVO> unprocessedSkipDays, Integer childMealPrice) {
        List<SkippedDaysVO> unprocessedBeforeProcessingInvoice = getUnproccessedSkipDaysFromInMemorySkipDays(unprocessedSkipDays);

        unprocessedBeforeProcessingInvoice.forEach(skippedDaysVO -> {
            int amountForCurrentSkipDaysPeriod = getAmountForSkippedDaysEntity(childMealPrice, skippedDaysVO);

            if (skipDaysAmountCanBeSubstractedFromInvoiceAmount(invoiceVO, amountForCurrentSkipDaysPeriod)) {
                substractSkipDaysAmountFromInvoicesAmountAndAddSetItAsProccessedInMemory(invoiceVO, skippedDaysVO, amountForCurrentSkipDaysPeriod);
            }
        });
    }
}
