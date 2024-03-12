package com.edusphere.controllers;

import com.edusphere.authorizationAnnotations.OwnerOrAdminOrParentPermission;
import com.edusphere.authorizationAnnotations.OwnerOrAdminPermission;
import com.edusphere.services.InvoiceService;
import com.edusphere.utils.AuthenticatedUserUtil;
import com.edusphere.vos.InvoiceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/invoice")
@Tag(name = "User Controller", description = "APIs for managing users")
@SecurityRequirement(name = "Bearer Authentication")
@OwnerOrAdminPermission
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final AuthenticatedUserUtil authenticatedUserUtil;

    public InvoiceController(InvoiceService invoiceService, AuthenticatedUserUtil authenticatedUserUtil) {
        this.invoiceService = invoiceService;
        this.authenticatedUserUtil = authenticatedUserUtil;
    }

    @GetMapping
    @Operation(summary = "Save invoice from organization", description = "Get all users from organization")
    public void saveInvoiceFile() {
       invoiceService.saveInvoiceToFile("./invoice.pdf");
    }


    @Operation(summary = "Get invoices by month", description = "Retrieve a list of invoices by month")
    @GetMapping("/child/{id}")
    @OwnerOrAdminPermission
    public List<InvoiceVO> getChildInvoicesByMonth(
            @Parameter(description = "ID of the child to update") @PathVariable("id") Integer childId,
            @Parameter(description = "Month of the invoices to retrieve in YYYY-MM format")
            @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        if(month == null){
            throw new IllegalArgumentException("Month must not be null");
        }
        Integer organizationId = authenticatedUserUtil.getCurrentUserOrganizationId();

        return invoiceService.getChildInvoicesByMonth(childId, month, organizationId);
    }

    @Operation(summary = "Get invoices by month", description = "Retrieve a list of invoices by month")
    @GetMapping("/parent/{id}")
    @OwnerOrAdminOrParentPermission
    public List<InvoiceVO> getParentInvoicesByMonth(
            @Parameter(description = "ID of the child to update") @PathVariable("id") Integer childId,
            @Parameter(description = "Month of the invoices to retrieve in YYYY-MM format")
            @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        if(month == null){
            throw new IllegalArgumentException("Month must not be null");
        }
        Integer organizationId = authenticatedUserUtil.getCurrentUserOrganizationId();

        return invoiceService.getParentInvoicesByMonth(childId, month, organizationId);
    }
}
