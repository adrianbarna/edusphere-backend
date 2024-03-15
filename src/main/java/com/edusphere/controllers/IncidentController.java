package com.edusphere.controllers;

import com.edusphere.authorizationAnnotations.TeacherOrAdminOrOwnerPermission;
import com.edusphere.services.IncidentService;
import com.edusphere.utils.AuthenticatedUserUtil;
import com.edusphere.vos.IncidentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/incidents")
@SecurityRequirement(name = "Bearer Authentication")
@TeacherOrAdminOrOwnerPermission
public class IncidentController {

    private final IncidentService incidentService;
    private final AuthenticatedUserUtil authenticatedUserUtil;

    public IncidentController(IncidentService incidentService, AuthenticatedUserUtil authenticatedUserUtil) {
        this.incidentService = incidentService;
        this.authenticatedUserUtil = authenticatedUserUtil;
    }

    @Operation(summary = "Get all incidents", description = "Retrieve a list of all incidents")
    @GetMapping
    public ResponseEntity<List<IncidentVO>> getAllIncidents() {
        Integer organizationId = authenticatedUserUtil.getCurrentUserOrganizationId();
        List<IncidentVO> incidents = incidentService.getAllIncidents(organizationId);
        return new ResponseEntity<>(incidents, HttpStatus.OK);
    }

    @Operation(summary = "Get an incident by ID", description = "Retrieve an incident by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<IncidentVO> getIncidentById(
            @Parameter(description = "ID of the incident to retrieve") @PathVariable("id") Integer id) {
        Integer organizationId = authenticatedUserUtil.getCurrentUserOrganizationId();
        IncidentVO incident = incidentService.getIncidentById(id, organizationId);
        return new ResponseEntity<>(incident, HttpStatus.OK);
    }

    @Operation(summary = "Create an incident", description = "Create a new incident")
    @PostMapping
    public ResponseEntity<IncidentVO> createIncident(
            @Parameter(description = "Incident data to create") @RequestBody IncidentVO incidentVO) {
        Integer organizationId = authenticatedUserUtil.getCurrentUserOrganizationId();
        IncidentVO createdIncident = incidentService.createIncident(incidentVO, organizationId);
        return new ResponseEntity<>(createdIncident, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an incident by ID", description = "Update an incident by its ID")
    @PutMapping("/{id}")
    public ResponseEntity<IncidentVO> updateIncident(
            @Parameter(description = "ID of the incident to update") @PathVariable("id") Integer id,
            @Parameter(description = "Incident data to update") @RequestBody IncidentVO incidentVO) {
        Integer organizationId = authenticatedUserUtil.getCurrentUserOrganizationId();
        IncidentVO updatedIncident = incidentService.updateIncident(id, incidentVO, organizationId);
        return new ResponseEntity<>(updatedIncident, HttpStatus.OK);
    }

    @Operation(summary = "Delete an incident by ID", description = "Delete an incident by its ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncident(
            @Parameter(description = "ID of the incident to delete") @PathVariable("id") Integer id) {
        Integer organizationId = authenticatedUserUtil.getCurrentUserOrganizationId();
        incidentService.deleteIncident(id, organizationId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
