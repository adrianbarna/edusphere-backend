package com.edusphere.controllers;

import com.edusphere.authorizationAnnotations.TeacherOrAdminOrOwnerPermission;
import com.edusphere.services.ChildService;
import com.edusphere.utils.AuthenticatedUserUtil;
import com.edusphere.vos.ChildVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/child")
@Tag(name = "Child Controller", description = "APIs for managing children")
@SecurityRequirement(name = "Bearer Authentication")
@TeacherOrAdminOrOwnerPermission
public class ChildController {

    private final ChildService childService;
    private final AuthenticatedUserUtil authenticatedUserUtil;

    public ChildController(ChildService childService, AuthenticatedUserUtil authenticatedUserUtil) {
        this.childService = childService;
        this.authenticatedUserUtil = authenticatedUserUtil;
    }

    @Operation(summary = "Get all children", description = "Retrieve a list of all children")
    @GetMapping
    public List<ChildVO> getAllChildren() {
        Integer organizationId = authenticatedUserUtil.getCurrentUserOrganizationId();
        return childService.getAllChildren(organizationId);
    }

    @Operation(summary = "Get a child by ID", description = "Retrieve a child by their ID")
    @GetMapping("/{childId}")
    public ChildVO getChildById(
            @Parameter(description = "ID of the child to retrieve") @PathVariable Integer childId) {
        Integer organizationId = authenticatedUserUtil.getCurrentUserOrganizationId();
        return childService.getChildById(childId, organizationId);
    }

    @Operation(summary = "Get children by parent ID", description = "Retrieve a list of children by parent ID")
    @GetMapping("/parent/{parentId}")
    public List<ChildVO> getChildrenById(
            @Parameter(description = "ID of the parent to retrieve children for") @PathVariable Integer parentId) {
        Integer organizationId = authenticatedUserUtil.getCurrentUserOrganizationId();
        return childService.getChildrenByParentId(parentId, organizationId);
    }

    @Operation(summary = "Create a child", description = "Create a new child")
    @PostMapping
    public ChildVO createChild(
            @Parameter(description = "Child data to create") @RequestBody ChildVO childVO) {
        Integer organizationId = authenticatedUserUtil.getCurrentUserOrganizationId();
        return childService.addChild(childVO, organizationId);
    }

    @Operation(summary = "Update a child by ID", description = "Update a child by their ID")
    @PutMapping("/{id}")
    public ChildVO updateChild(
            @Parameter(description = "ID of the child to update") @PathVariable Integer id,
            @Parameter(description = "Child data to update") @RequestBody ChildVO childVO) {
        Integer organizationId = authenticatedUserUtil.getCurrentUserOrganizationId();
        return childService.updateChild(id, childVO, organizationId);
    }

    @Operation(summary = "Delete a child by ID", description = "Delete a child by their ID")
    @DeleteMapping("/{id}")
    public void deleteChild(
            @Parameter(description = "ID of the child to delete") @PathVariable Integer id) {
        Integer organizationId = authenticatedUserUtil.getCurrentUserOrganizationId();
        childService.deleteChild(id, organizationId);
    }
}
