package com.edusphere.controllers;

import com.edusphere.authorizationAnnotations.OwnerPermission;
import com.edusphere.services.OrganizationService;
import com.edusphere.vos.OrganizationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organization")
@Tag(name = "Organization Controller", description = "APIs for managing organizations")
@SecurityRequirement(name = "Bearer Authentication")
@OwnerPermission
public class OrganizationController {
    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping
    @Operation(summary = "Add an organization")
    public ResponseEntity<OrganizationVO> createOrganization(@RequestBody OrganizationVO organizationVO) {
        OrganizationVO organizationResponseVO = organizationService.addOrganization(organizationVO);
        return new ResponseEntity<>(organizationResponseVO, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all organizations")
    public List<OrganizationVO> getAllOrganizations() {
        return organizationService.getAllOrganizations();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an organization by ID")
    public OrganizationVO getOrganizationById(@PathVariable("id") @Parameter(description = "Organization ID") Integer id) {
        return organizationService.getOrganizationById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an organization by ID")
    public void updateOrganization(@PathVariable("id") @Parameter(description = "Organization ID") Integer id, @RequestBody OrganizationVO organizationVO) {
        organizationService.updateOrganization(id, organizationVO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an organization by ID")
    public void deleteOrganization(@PathVariable("id") @Parameter(description = "Organization ID") Integer id) {
        organizationService.deleteOrganization(id);
    }
}
