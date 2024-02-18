package com.edusphere.controllers;

import com.edusphere.authorizationAnnotations.TeacherOrAdminOrOwnerPermission;
import com.edusphere.services.ClassService;
import com.edusphere.utils.AuthenticatedUserUtil;
import com.edusphere.vos.ClassVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/class")
@Tag(name = "Class Controller", description = "APIs for managing classes")
@SecurityRequirement(name = "Bearer Authentication")
@TeacherOrAdminOrOwnerPermission
public class ClassController {

    private final ClassService classService;
    private final AuthenticatedUserUtil authenticatedUserUtil;

    public ClassController(ClassService classService, AuthenticatedUserUtil authenticatedUserUtil) {
        this.classService = classService;
        this.authenticatedUserUtil = authenticatedUserUtil;
    }

    @GetMapping
    @Operation(summary = "Get all classes", description = "Get a list of all classes")
    public ResponseEntity<List<ClassVO>> getAllClasses() {
        Integer currentUserOrganizationId = authenticatedUserUtil.getCurrentUserOrganizationId();
        List<ClassVO> classes = classService.getAllClasses(currentUserOrganizationId);
        return new ResponseEntity<>(classes, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get class by ID", description = "Get a class by its ID")
    public ResponseEntity<ClassVO> getClassById(@PathVariable Integer id) {
        Integer currentUserOrganizationId = authenticatedUserUtil.getCurrentUserOrganizationId();
        ClassVO classVO = classService.getClassById(id, currentUserOrganizationId);
        return new ResponseEntity<>(classVO, HttpStatus.OK);
    }

    @PostMapping
    @Operation(summary = "Create a new class", description = "Create a new class")
    public ResponseEntity<ClassVO> createClass(@RequestBody ClassVO classVO) {
        Integer currentUserOrganizationId = authenticatedUserUtil.getCurrentUserOrganizationId();
        ClassVO createdClass = classService.createClass(classVO, currentUserOrganizationId);

        return new ResponseEntity<>(createdClass, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update class by ID", description = "Update a class by its ID")
    public ResponseEntity<ClassVO> updateClass(@PathVariable Integer id, @RequestBody ClassVO classVO) {
        Integer currentUserOrganizationId = authenticatedUserUtil.getCurrentUserOrganizationId();
        ClassVO updatedClass = classService.updateClass(id, classVO, currentUserOrganizationId);
        return new ResponseEntity<>(updatedClass, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete class by ID", description = "Delete a class by its ID")
    public ResponseEntity<Void> deleteClass(@PathVariable Integer id) {
        try {
            Integer currentUserOrganizationId = authenticatedUserUtil.getCurrentUserOrganizationId();
            classService.deleteClass(id, currentUserOrganizationId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    //TODO create controller to assign a teacher to a class
}
