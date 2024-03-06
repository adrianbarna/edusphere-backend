package com.edusphere.controllers;

import com.edusphere.authorizationAnnotations.OwnerOrAdminPermission;
import com.edusphere.services.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/invoice")
@Tag(name = "User Controller", description = "APIs for managing users")
@SecurityRequirement(name = "Bearer Authentication")
@OwnerOrAdminPermission
public class InvoiceController {

    private final InvoiceService invoiceService;

    @Autowired
    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
    @Operation(summary = "Get all users from organization", description = "Get all users from organization")
    public void getAllUsers() {
       invoiceService.saveInvoiceToFile("./invoice.pdf");
    }

}
