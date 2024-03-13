package com.edusphere.controllers;

import com.edusphere.authorizationAnnotations.OwnerOrAdminOrParentPermission;
import com.edusphere.authorizationAnnotations.OwnerOrAdminPermission;
import com.edusphere.services.PaymentService;
import com.edusphere.utils.AuthenticatedUserUtil;
import com.edusphere.vos.PaymentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/payment")
@Tag(name = "User Controller", description = "APIs for managing users")
@SecurityRequirement(name = "Bearer Authentication")
@OwnerOrAdminPermission
public class PaymentController {

    private final PaymentService paymentService;
    private final AuthenticatedUserUtil authenticatedUserUtil;

    public PaymentController(PaymentService paymentService, AuthenticatedUserUtil authenticatedUserUtil) {
        this.paymentService = paymentService;
        this.authenticatedUserUtil = authenticatedUserUtil;
    }

    @GetMapping
    @Operation(summary = "Save invoice from organization", description = "Get all users from organization")
    public void saveInvoiceFile() {
        paymentService.saveInvoiceToFile("./invoice.pdf");
    }


    @Operation(summary = "Get payments by month for child", description = "Retrieve a list of payments by month" +
            "for child")
    @GetMapping("/child/{id}")
    @OwnerOrAdminPermission
    public List<PaymentVO> getChildPaymentsByMonth(
            @Parameter(description = "ID of the child to update") @PathVariable("id") Integer childId,
            @Parameter(description = "Month of the payments to retrieve in YYYY-MM format")
            @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        if (month == null) {
            throw new IllegalArgumentException("Month must not be null");
        }
        Integer organizationId = authenticatedUserUtil.getCurrentUserOrganizationId();

        return paymentService.getChildPaymentsByMonth(childId, month, organizationId);
    }

    @Operation(summary = "Get payments by month for parent",
            description = "Retrieve a list of payments by month for parent")
    @GetMapping("/parent/{id}")
    @OwnerOrAdminOrParentPermission
    public List<PaymentVO> getParentPaymentsByMonth(
            @Parameter(description = "ID of the child to update") @PathVariable("id") Integer childId,
            @Parameter(description = "Month of the payments to retrieve in YYYY-MM format")
            @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        if (month == null) {
            throw new IllegalArgumentException("Month must not be null");
        }
        Integer organizationId = authenticatedUserUtil.getCurrentUserOrganizationId();

        return paymentService.getParentPaymentsByMonth(childId, month, organizationId);
    }

    @Operation(summary = "Mark payment as paid ",
            description = "Mark payment as paid or unpaid")
    @PutMapping("/markAsPaid/{id}/{isPaid}")
    @OwnerOrAdminPermission
    public PaymentVO markPayment(
            @Parameter(description = "ID of the child to update") @PathVariable("id") Integer paymentId,
            @Parameter(description = "Marker to mark as paid or not") @PathVariable("isPaid") boolean isPaid) {
        Integer organizationId = authenticatedUserUtil.getCurrentUserOrganizationId();

        return paymentService.markPaymentAsPaidOrUnpaid(paymentId, isPaid, organizationId);
    }
}
