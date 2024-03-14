package com.edusphere.controllers;

import com.edusphere.authorizationAnnotations.OwnerOrAdminPermission;
import com.edusphere.services.PaymentService;
import com.edusphere.utils.AuthenticatedUserUtil;
import com.edusphere.vos.PaymentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "Mark payment as paid ",
            description = "Mark payment as paid or unpaid")
    @PutMapping("/markAsPaid/{id}/{isPaid}")
    @OwnerOrAdminPermission
    public PaymentVO markPayment(
            @Parameter(description = "ID of the payment to update") @PathVariable("id") Integer paymentId,
            @Parameter(description = "Marker to mark as paid or not") @PathVariable("isPaid") boolean isPaid) {
        Integer organizationId = authenticatedUserUtil.getCurrentUserOrganizationId();

        return paymentService.markPaymentAsPaidOrUnpaid(paymentId, isPaid, organizationId);
    }

    @Operation(summary = "Mark payment as paid ",
            description = "Mark payment as paid or unpaid")
    @PostMapping("/generatePayment/{childId}")
    @OwnerOrAdminPermission
    public PaymentVO generatePayment(
            @Parameter(description = "ID of the child to update") @PathVariable("childId") Integer childId) {
        Integer organizationId = authenticatedUserUtil.getCurrentUserOrganizationId();

        return paymentService.generatePaymentForMonth(childId, organizationId);
    }
}
