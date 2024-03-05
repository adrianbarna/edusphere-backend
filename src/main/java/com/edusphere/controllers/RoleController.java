package com.edusphere.controllers;

import com.edusphere.authorizationAnnotations.OwnerOrAdminPermission;
import com.edusphere.services.RoleService;
import com.edusphere.utils.AuthenticatedUserUtil;
import com.edusphere.vos.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/role")
@Tag(name = "Role Controller", description = "APIs for managing roles")
@SecurityRequirement(name = "Bearer Authentication")
@OwnerOrAdminPermission
public class RoleController {

    private final RoleService roleService;
    private final AuthenticatedUserUtil authenticatedUserUtil;

    public RoleController(RoleService roleService, AuthenticatedUserUtil authenticatedUserUtil) {
        this.roleService = roleService;
        this.authenticatedUserUtil = authenticatedUserUtil;
    }

    @GetMapping
    @Operation(summary = "Get all roles")
    public List<RoleVO> getAllRoles() {
        Integer organizationId = authenticatedUserUtil.getCurrentUserOrganizationId();
        return roleService.getAllRoles(organizationId);
    }

    @GetMapping("/{roleId}")
    @Operation(summary = "Get a role by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = RoleVO.class))
            }),
            @ApiResponse(responseCode = "404", description = "Role not found", content = @Content)
    })
    public RoleVO getRoleById(@PathVariable("roleId") @Parameter(description = "Role ID") Integer roleId) {
        Integer organizationId = authenticatedUserUtil.getCurrentUserOrganizationId();
        return roleService.getRoleById(roleId, organizationId);
    }

    @PostMapping
    @Operation(summary = "Create a role")
    public ResponseEntity<RoleVO> createRole(@RequestBody CreateUpdateRoleVO roleVO) {
        Integer organizationId = authenticatedUserUtil.getCurrentUserOrganizationId();
        RoleVO roleResponseVO = roleService.createRole(roleVO, organizationId);
        return new ResponseEntity<>(roleResponseVO, HttpStatus.CREATED);
    }

    @PutMapping("/{roleId}")
    @Operation(summary = "Update a role by ID")
    public RoleVO updateRole(@PathVariable("roleId") @Parameter(description = "Role ID") Integer roleId, @RequestBody CreateUpdateRoleVO roleVO) {
        Integer organizationId = authenticatedUserUtil.getCurrentUserOrganizationId();
        return roleService.updateRole(roleId, roleVO, organizationId);
    }

    @DeleteMapping("/{roleId}")
    @Operation(summary = "Delete a role by ID")
    public boolean deleteRole(@PathVariable("roleId") @Parameter(description = "Role ID") Integer roleId) {
        Integer organizationId = authenticatedUserUtil.getCurrentUserOrganizationId();
        return roleService.deleteRole(roleId, organizationId);
    }

    @PutMapping("/assign")
    @Operation(summary = "Assign roles to a user")
    public ResponseEntity<UserResponseVO> assignRoleToUser(@RequestBody AssignRoleRequestWrapperVO assignRoleRequestWrapperVO) {
        Integer organizationId = authenticatedUserUtil.getCurrentUserOrganizationId();
        return ResponseEntity.ok(roleService.assignRoleToUser(assignRoleRequestWrapperVO, organizationId));
    }

    @PostMapping("/changeRoles")
    @Operation(summary = "Rewrite all roles for a user")
    public ResponseEntity<UserResponseVO> changeRolesToUser(@RequestBody AssignRoleRequestWrapperVO assignRoleRequestWrapperVO) {
        Integer organizationId = authenticatedUserUtil.getCurrentUserOrganizationId();
        return ResponseEntity.ok(roleService.changeRolesToUser(assignRoleRequestWrapperVO, organizationId));
    }
}
