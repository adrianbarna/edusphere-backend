package com.edusphere.controllers;

import com.edusphere.authorizationAnnotations.OwnerOrAdminPermission;
import com.edusphere.exceptions.UserNotFoundException;
import com.edusphere.services.UserService;
import com.edusphere.utils.AuthenticatedUserUtil;
import com.edusphere.vos.UserRequestVO;
import com.edusphere.vos.UserResponseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@Tag(name = "User Controller", description = "APIs for managing users")
@SecurityRequirement(name = "Bearer Authentication")
@OwnerOrAdminPermission
public class UserController {

    private final UserService userService;
    private final AuthenticatedUserUtil authenticatedUserUtil;

    @Autowired
    public UserController(UserService userService, AuthenticatedUserUtil authenticatedUserUtil) {
        this.userService = userService;
        this.authenticatedUserUtil = authenticatedUserUtil;
    }

    @GetMapping
    @Operation(summary = "Get all users from organization", description = "Get all users from organization")
    public ResponseEntity<List<UserResponseVO>> getAllUsers() {
        Integer organizationId = authenticatedUserUtil.getCurrentUserOrganizationId();

        if (organizationId == null) {
            throw new IllegalStateException("User-ul logat nu este asignat niciunei organizatii!");
        }

        List<UserResponseVO> users = userService.getAllUsersByOrganizationWithoutPasswordField(organizationId);
        return ResponseEntity.ok(users);
    }

    @PostMapping
    @Operation(summary = "Create a new user", description = "Create a new user")
    public ResponseEntity<UserResponseVO> createUser(@RequestBody UserRequestVO userRequestVO) {
        Integer currentOrganizationId = authenticatedUserUtil.getCurrentUserOrganizationId();

        UserResponseVO createdUser = userService.createUser(userRequestVO, currentOrganizationId);
        if (createdUser != null) {
            return ResponseEntity.ok(createdUser);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }


    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Get a user by their ID")
    public ResponseEntity<UserResponseVO> getUserById(@PathVariable Integer id) {
        Integer currentOrganizationId = authenticatedUserUtil.getCurrentUserOrganizationId();
        UserResponseVO user = userService.getUserById(id, currentOrganizationId);

        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user by ID", description = "Update a user by their ID")
    public ResponseEntity<UserResponseVO> updateUserByAdmin(@PathVariable Integer id, @RequestBody UserRequestVO userRequestVO) {
        Integer currentOrganizationId = authenticatedUserUtil.getCurrentUserOrganizationId();
        UserResponseVO updatedUser = userService.updateUser(id, userRequestVO, currentOrganizationId);
        if (updatedUser != null) {
            return ResponseEntity.ok(updatedUser);
        } else {
            throw new UserNotFoundException("Nu s-a gasit niciun user cu username-ul: " + userRequestVO.getUsername());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user by ID", description = "Delete a user by their ID")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        Integer currentOrganizationId = authenticatedUserUtil.getCurrentUserOrganizationId();
        boolean deleted = userService.deleteUser(id, currentOrganizationId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            throw new UserNotFoundException(id);
        }
    }
}
