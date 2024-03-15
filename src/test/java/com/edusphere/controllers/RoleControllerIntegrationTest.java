package com.edusphere.controllers;

import com.edusphere.controllers.utils.OrganizationTestUtils;
import com.edusphere.controllers.utils.RoleTestUtils;
import com.edusphere.controllers.utils.TokenTestUtils;
import com.edusphere.controllers.utils.UserTestUtils;
import com.edusphere.entities.OrganizationEntity;
import com.edusphere.entities.RoleEntity;
import com.edusphere.entities.UserEntity;
import com.edusphere.exceptions.RoleNotFoundException;
import com.edusphere.repositories.RoleRepository;
import com.edusphere.repositories.UserRepository;
import com.edusphere.vos.AssignRoleRequestWrapperVO;
import com.edusphere.vos.CreateUpdateRoleVO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.edusphere.controllers.utils.StringTestUtils.asJsonString;
import static com.edusphere.controllers.utils.StringTestUtils.generateRandomString;
import static com.edusphere.enums.RolesEnum.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@AutoConfigureMockMvc
@SpringBootTest(properties = "spring.config.name=application-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = "spring.config.location=classpath:/application-test.properties")
 class RoleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationTestUtils organizationUtils;

    @Autowired
    private RoleTestUtils roleUtils;

    @Autowired
    private UserTestUtils userUtils;

    @Autowired
    private TokenTestUtils tokenUtils;

    private final List<UserEntity> allowedUsersToCallTheEndpoint = new ArrayList<>();
    private final List<UserEntity> notAllowedUsersToCallTheEndpoint = new ArrayList<>();

    @BeforeAll
     void setup() {
        OrganizationEntity organizationEntity = organizationUtils.saveOrganization();
        RoleEntity adminRole = roleUtils.saveRole(ADMIN.getName(), organizationEntity);
        RoleEntity ownerRole = roleUtils.saveRole(OWNER.getName(), organizationEntity);
        RoleEntity teacherRole = roleUtils.saveRole(TEACHER.getName(), organizationEntity);
        RoleEntity parentRole = roleUtils.saveRole(PARENT.getName(), organizationEntity);
        allowedUsersToCallTheEndpoint.add(userUtils.saveUser(generateRandomString(), "123456", organizationEntity, adminRole));
        allowedUsersToCallTheEndpoint.add(userUtils.saveUser(generateRandomString(), "123456", organizationEntity, ownerRole));
        notAllowedUsersToCallTheEndpoint.add(userUtils.saveUser(generateRandomString(), "123456", organizationEntity, teacherRole));
        notAllowedUsersToCallTheEndpoint.add(userUtils.saveUser(generateRandomString(), "123456", organizationEntity, parentRole));
    }

    @Test
     void getAllRoles() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                getAllRolesWhenCalledByUser(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getAllRolesWhenCalledByUser(UserEntity userEntity) throws Exception {
        OrganizationEntity anotherEntity = organizationUtils.saveOrganization();
        RoleEntity roleFromAnotherOrganization = roleUtils.saveRole(generateRandomString(), anotherEntity);
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), "123456");

        List<RoleEntity> organizationRoles = roleRepository.findByOrganizationId(userEntity.getOrganization().getId());

        mockMvc.perform(MockMvcRequestBuilders.get("/role")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].name", hasSize(organizationRoles.size())))
                .andExpect(jsonPath("$.[*].name", not(hasItem(roleFromAnotherOrganization.getName()))));
    }

    @Test
     void getAllRolesShouldFailForNotAllowedRoles() {
        try {
            for (UserEntity notAllowedUser : notAllowedUsersToCallTheEndpoint) {
                getAllRolesWhenCalledByUserShouldFailForNotAllowedRoles(notAllowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getAllRolesWhenCalledByUserShouldFailForNotAllowedRoles(UserEntity userEntity) throws Exception {
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), "123456");


        mockMvc.perform(MockMvcRequestBuilders.get("/role")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].name", hasSize(0)));
    }

    @Test
     void getRoleById() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                getRoleByIdWhenCalledByUser(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getRoleByIdWhenCalledByUser(UserEntity userEntity) throws Exception {
        RoleEntity aRole = roleUtils.saveRole(generateRandomString(), userEntity.getOrganization());

        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), "123456");

        mockMvc.perform(MockMvcRequestBuilders.get("/role/" + aRole.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(aRole.getName()));
    }

    @Test
     void getRoleByIdShouldFailForNotAllowedRoles() {
        try {
            for (UserEntity notAllowedUser : notAllowedUsersToCallTheEndpoint) {
                getRoleByIdWhenCalledByUserShouldFailForNotAllowedRoles(notAllowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getRoleByIdWhenCalledByUserShouldFailForNotAllowedRoles(UserEntity userEntity) throws Exception {
        RoleEntity aRole = roleUtils.saveRole(generateRandomString(), userEntity.getOrganization());

        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), "123456");

        mockMvc.perform(MockMvcRequestBuilders.get("/role/" + aRole.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].name", hasSize(0)));
    }

    @Test
     void getRoleByIdShouldFailWhenTakingFromAnotherOrganization() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                getRoleByIdWhenCalledByUserShouldFailWhenTakingFromAnotherOrganization(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getRoleByIdWhenCalledByUserShouldFailWhenTakingFromAnotherOrganization(UserEntity userEntity) throws Exception {
        OrganizationEntity anotherEntity = organizationUtils.saveOrganization();
        RoleEntity roleFromAnotherOrganization = roleUtils.saveRole(generateRandomString(), anotherEntity);

        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), "123456");

        mockMvc.perform(MockMvcRequestBuilders.get("/role/" + roleFromAnotherOrganization.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ups! A aparut o eroare!"))
                .andExpect(jsonPath("$.message").value("Rolul cu id-ul " + roleFromAnotherOrganization.getId() + " este invalid."));
    }

    @Test
     void createRole() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                createRoleWhenCalledByUser(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void createRoleWhenCalledByUser(UserEntity userEntity) throws Exception {
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), "123456");
        CreateUpdateRoleVO roleVO = new CreateUpdateRoleVO();
        roleVO.setName(generateRandomString());

        mockMvc.perform(MockMvcRequestBuilders.post("/role")
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(roleVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(roleVO.getName()));

        RoleEntity createdRole = roleRepository.findByName(roleVO.getName()).orElse(null);
        assertNotNull(createdRole);
    }

    @Test
     void createRoleShouldFailForNotAllowedRoles() {
        try {
            for (UserEntity notAllowedUser : notAllowedUsersToCallTheEndpoint) {
                createRoleShouldFailForNotAllowedRoles(notAllowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void createRoleShouldFailForNotAllowedRoles(UserEntity userEntity) throws Exception {
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), "123456");
        CreateUpdateRoleVO roleVO = new CreateUpdateRoleVO();
        roleVO.setName(generateRandomString());

        mockMvc.perform(MockMvcRequestBuilders.post("/role")
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(roleVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].name", hasSize(0)));

        RoleEntity createdRole = roleRepository.findByName(roleVO.getName()).orElse(null);
        assertNull(createdRole);
    }

    @Test
     void updateRole() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                updateRoleWhenCalledByUser(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void updateRoleWhenCalledByUser(UserEntity userEntity) throws Exception {
        RoleEntity role = roleUtils.saveRole(generateRandomString(), userEntity.getOrganization());
        assertNotNull(role);

        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), "123456");
        CreateUpdateRoleVO roleVO = new CreateUpdateRoleVO();
        roleVO.setName(generateRandomString());

        mockMvc.perform(MockMvcRequestBuilders.put("/role/" + role.getId())
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(roleVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(roleVO.getName()));

        RoleEntity updatedRole = roleRepository.findByName(roleVO.getName()).orElse(null);
        assertNotNull(updatedRole);
    }

    @Test
     void updateRoleShouldFailWhenRoleFromAnotherOrganization() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                updateRoleShouldFailWhenRoleFromAnotherOrganization(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void updateRoleShouldFailWhenRoleFromAnotherOrganization(UserEntity userEntity) throws Exception {
        OrganizationEntity anotherOrganization = organizationUtils.saveOrganization();
        RoleEntity role = roleUtils.saveRole(generateRandomString(), anotherOrganization);
        assertNotNull(role);

        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), "123456");
        CreateUpdateRoleVO roleVO = new CreateUpdateRoleVO();
        roleVO.setName(generateRandomString());

        mockMvc.perform(MockMvcRequestBuilders.put("/role/" + role.getId())
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(roleVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Ups! A aparut o eroare!"))
                .andExpect(jsonPath("$.message").value("Rolul cu id-ul " + role.getId()
                        + " este invalid."));

        RoleEntity updatedRole = roleRepository.findByName(roleVO.getName()).orElse(null);
        assertNull(updatedRole);
    }


    @Test
     void deleteRole() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                deleteRoleWhenCalledByUser(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void deleteRoleWhenCalledByUser(UserEntity userEntity) throws Exception {
        RoleEntity role = roleUtils.saveRole(generateRandomString(), userEntity.getOrganization());
        assertNotNull(role);

        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), "123456");

        mockMvc.perform(MockMvcRequestBuilders.delete("/role/" + role.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        RoleEntity deletedRole = roleRepository.findByName(role.getName()).orElse(null);
        assertNull(deletedRole);
    }

    @Test
     void deleteRole_shouldFailWhenDeletingFromAnotherOrganization() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                deleteRoleShouldFailWhenCalledByUserFromAnotherOrganization(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void deleteRoleShouldFailWhenCalledByUserFromAnotherOrganization(UserEntity userEntity) throws Exception {
        OrganizationEntity anotherOrganization = organizationUtils.saveOrganization();
        RoleEntity role = roleUtils.saveRole(generateRandomString(), anotherOrganization);
        assertNotNull(role);

        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), "123456");

        mockMvc.perform(MockMvcRequestBuilders.delete("/role/" + role.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ups! A aparut o eroare!"))
                .andExpect(jsonPath("$.message").value("Rolul cu id-ul " + role.getId() + " este invalid."));

        RoleEntity deletedRole = roleRepository.findByName(role.getName()).orElse(null);
        assertNotNull(deletedRole);
    }

    @Test
     void deleteRole_shouldFailForNotAllowedUser() {
        try {
            for (UserEntity notAllowedUser : notAllowedUsersToCallTheEndpoint) {
                deleteRole_shouldFailForNotAllowedUser(notAllowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void deleteRole_shouldFailForNotAllowedUser(UserEntity userEntity) throws Exception {
        OrganizationEntity anotherOrganization = organizationUtils.saveOrganization();
        RoleEntity role = roleUtils.saveRole(generateRandomString(), anotherOrganization);
        assertNotNull(role);

        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), "123456");

        mockMvc.perform(MockMvcRequestBuilders.delete("/role/" + role.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value("Nu aveti suficiente drepturi pentru aceasta operatiune!"));

        RoleEntity deletedRole = roleRepository.findByName(role.getName()).orElse(null);
        assertNotNull(deletedRole);
    }

    @Test
    @Transactional
     void assignRoleToUser() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                assignRoleToUserWhenCalledByUser(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void assignRoleToUserWhenCalledByUser(UserEntity userEntity) throws Exception {
        RoleEntity role = roleUtils.saveRole(generateRandomString(), userEntity.getOrganization());
        UserEntity userToAssignANewRole = userUtils.saveUser(generateRandomString(), generateRandomString(),
                userEntity.getOrganization(), userEntity.getRoles().stream().findFirst().orElseThrow(() -> new RoleNotFoundException("Rolul nu a fost gasit")));
        assertNotNull(role);


        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), "123456");
        AssignRoleRequestWrapperVO assignRoleRequestWrapperVO = new AssignRoleRequestWrapperVO();
        assignRoleRequestWrapperVO.setUserId(userToAssignANewRole.getId());
        assignRoleRequestWrapperVO.getRoleIds().add(role.getId());

        mockMvc.perform(MockMvcRequestBuilders.put("/role/assign")
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(assignRoleRequestWrapperVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        UserEntity userWithAssignedRole = userRepository.findById(userToAssignANewRole.getId()).orElse(null);
        assertNotNull(userWithAssignedRole);
        assertTrue(userWithAssignedRole.getRoles().contains(role));
    }

    @Test
    @Transactional
     void assignRoleFromAnotherOrganizationToUserShouldFail() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                assignRoleFromAnotherOrganizationToUserShouldFail(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void assignRoleFromAnotherOrganizationToUserShouldFail(UserEntity userEntity) throws Exception {
        OrganizationEntity anotherOrganization = organizationUtils.saveOrganization();
        RoleEntity role = roleUtils.saveRole(generateRandomString(), anotherOrganization);
        UserEntity userToAssignANewRole = userUtils.saveUser(generateRandomString(), generateRandomString(),
                userEntity.getOrganization(), userEntity.getRoles().stream().findFirst().orElseThrow(() -> new RoleNotFoundException("Rolul nu a fost gasit")));
        assertNotNull(role);


        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), "123456");
        AssignRoleRequestWrapperVO assignRoleRequestWrapperVO = new AssignRoleRequestWrapperVO();
        assignRoleRequestWrapperVO.setUserId(userToAssignANewRole.getId());
        assignRoleRequestWrapperVO.getRoleIds().add(role.getId());

        mockMvc.perform(MockMvcRequestBuilders.put("/role/assign")
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(assignRoleRequestWrapperVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ups! A aparut o eroare!"))
                .andExpect(jsonPath("$.message").value("Rolul cu id-ul " + role.getId() + " este invalid."));

        UserEntity userWithAssignedRole = userRepository.findById(userToAssignANewRole.getId()).orElse(null);
        assertNotNull(userWithAssignedRole);
        assertFalse(userWithAssignedRole.getRoles().contains(role));
    }

    @Test
    @Transactional
     void assignRole_shouldFailForNotAllowedUser() {
        try {
            for (UserEntity notAllowedUser : notAllowedUsersToCallTheEndpoint) {
                assignRole_shouldFailForNotAllowedUser(notAllowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void assignRole_shouldFailForNotAllowedUser(UserEntity userEntity) throws Exception {
        OrganizationEntity anotherOrganization = organizationUtils.saveOrganization();
        RoleEntity role = roleUtils.saveRole(generateRandomString(), anotherOrganization);
        UserEntity userToAssignANewRole = userUtils.saveUser(generateRandomString(), generateRandomString(),
                userEntity.getOrganization(), userEntity.getRoles().stream().findFirst().orElseThrow(() -> new RoleNotFoundException("Rolul nu a fost gasit")));
        assertNotNull(role);


        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), "123456");
        AssignRoleRequestWrapperVO assignRoleRequestWrapperVO = new AssignRoleRequestWrapperVO();
        assignRoleRequestWrapperVO.setUserId(userToAssignANewRole.getId());
        assignRoleRequestWrapperVO.getRoleIds().add(role.getId());

        mockMvc.perform(MockMvcRequestBuilders.put("/role/assign")
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(assignRoleRequestWrapperVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(jsonPath("$.message").value("Nu aveti suficiente drepturi pentru aceasta " +
                        "operatiune!"));

        UserEntity userWithAssignedRole = userRepository.findById(userToAssignANewRole.getId()).orElse(null);
        assertNotNull(userWithAssignedRole);
        assertFalse(userWithAssignedRole.getRoles().contains(role));
    }

    @Test
    @Transactional
     void assignRoleToUserFromAnotherOrganizationShouldFail() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                assignRoleToUserFromAnotherOrganizationShouldFail(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void assignRoleToUserFromAnotherOrganizationShouldFail(UserEntity userEntity) throws Exception {
        OrganizationEntity anotherOrganization = organizationUtils.saveOrganization();
        RoleEntity role = roleUtils.saveRole(generateRandomString(), userEntity.getOrganization());
        UserEntity userFromAnotherOrganization = userUtils.saveUser(generateRandomString(), generateRandomString(),
                anotherOrganization, userEntity.getRoles().stream().findFirst().orElseThrow(() -> new RoleNotFoundException("Rolul nu a fost gasit")));
        assertNotNull(role);


        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), "123456");
        AssignRoleRequestWrapperVO assignRoleRequestWrapperVO = new AssignRoleRequestWrapperVO();
        assignRoleRequestWrapperVO.setUserId(userFromAnotherOrganization.getId());
        assignRoleRequestWrapperVO.getRoleIds().add(role.getId());

        mockMvc.perform(MockMvcRequestBuilders.put("/role/assign")
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(assignRoleRequestWrapperVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ups! A aparut o eroare!"))
                .andExpect(jsonPath("$.message").value("Userul cu id-ul "
                        + userFromAnotherOrganization.getId() + " este invalid pentru organizatia " + userEntity.getOrganization().getId()));

        UserEntity userWithAssignedRole = userRepository.findById(userFromAnotherOrganization.getId()).orElse(null);
        assertNotNull(userWithAssignedRole);
        assertFalse(userWithAssignedRole.getRoles().contains(role));
    }

    @Test
    @Transactional
     void changeRolesToUser() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                changeRolesToUserWhenCalledByUser(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void changeRolesToUserWhenCalledByUser(UserEntity userEntity) throws Exception {
        RoleEntity role = roleUtils.saveRole(generateRandomString(), userEntity.getOrganization());
        assertNotNull(role);

        UserEntity userToChangeTheRole = userUtils.saveUser(generateRandomString(), generateRandomString(),
                userEntity.getOrganization(), userEntity.getRoles().stream().findFirst().orElseThrow(() -> new RoleNotFoundException("Rolul nu a fost gasit")));

        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), "123456");
        AssignRoleRequestWrapperVO assignRoleRequestWrapperVO = new AssignRoleRequestWrapperVO();
        assignRoleRequestWrapperVO.setUserId(userToChangeTheRole.getId());
        assignRoleRequestWrapperVO.setRoleIds(List.of(role.getId())); // Change roles to only "ADMIN"

        mockMvc.perform(MockMvcRequestBuilders.post("/role/changeRoles")
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(assignRoleRequestWrapperVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        UserEntity userWithChangedRoles = userRepository.findById(userToChangeTheRole.getId()).orElse(null);
        assertNotNull(userWithChangedRoles);
        assertEquals(1, userWithChangedRoles.getRoles().size());
        assertTrue(userWithChangedRoles.getRoles().contains(role));
    }

    @Test
    @Transactional
     void changeRolesToUserShouldFailWhenAddingRoleFromAnotherOrganization() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                changeRolesToUserShouldFailWhenAddingRoleFromAnotherOrganization(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void changeRolesToUserShouldFailWhenAddingRoleFromAnotherOrganization(UserEntity userEntity) throws Exception {
        OrganizationEntity anotherOrganization = organizationUtils.saveOrganization();
        RoleEntity roleFromAnotherOrganization = roleUtils.saveRole(generateRandomString(), anotherOrganization);
        assertNotNull(roleFromAnotherOrganization);

        UserEntity userToChangeTheRole = userUtils.saveUser(generateRandomString(), generateRandomString(),
                userEntity.getOrganization(), userEntity.getRoles().stream().findFirst().orElseThrow(() -> new RoleNotFoundException("Rolul nu a fost gasit")));

        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), "123456");
        AssignRoleRequestWrapperVO assignRoleRequestWrapperVO = new AssignRoleRequestWrapperVO();
        assignRoleRequestWrapperVO.setUserId(userToChangeTheRole.getId());
        assignRoleRequestWrapperVO.setRoleIds(List.of(roleFromAnotherOrganization.getId())); // Change roles to only "ADMIN"

        mockMvc.perform(MockMvcRequestBuilders.post("/role/changeRoles")
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(assignRoleRequestWrapperVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ups! A aparut o eroare!"))
                .andExpect(jsonPath("$.message").value("Rolul cu id-ul " + roleFromAnotherOrganization.getId() + " este invalid."));

        UserEntity userWithChangedRoles = userRepository.findById(userToChangeTheRole.getId()).orElse(null);
        assertNotNull(userWithChangedRoles);
        assertEquals(1, userWithChangedRoles.getRoles().size());
        assertFalse(userWithChangedRoles.getRoles().contains(roleFromAnotherOrganization));
    }

    @Test
    @Transactional
     void changeRoles_shouldFailForNotAllowedUser() {
        try {
            for (UserEntity notAllowedUser : notAllowedUsersToCallTheEndpoint) {
                changeRoles_shouldFailForNotAllowedUser(notAllowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void changeRoles_shouldFailForNotAllowedUser(UserEntity userEntity) throws Exception {
        OrganizationEntity anotherOrganization = organizationUtils.saveOrganization();
        RoleEntity roleFromAnotherOrganization = roleUtils.saveRole(generateRandomString(), anotherOrganization);
        assertNotNull(roleFromAnotherOrganization);

        UserEntity userToChangeTheRole = userUtils.saveUser(generateRandomString(), generateRandomString(),
                userEntity.getOrganization(), userEntity.getRoles().stream().findFirst().orElseThrow(() -> new RoleNotFoundException("Rolul nu a fost gasit")));

        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), "123456");
        AssignRoleRequestWrapperVO assignRoleRequestWrapperVO = new AssignRoleRequestWrapperVO();
        assignRoleRequestWrapperVO.setUserId(userToChangeTheRole.getId());
        assignRoleRequestWrapperVO.setRoleIds(List.of(roleFromAnotherOrganization.getId())); // Change roles to only "ADMIN"

        mockMvc.perform(MockMvcRequestBuilders.post("/role/changeRoles")
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(assignRoleRequestWrapperVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(jsonPath("$.message").value("Nu aveti suficiente drepturi pentru aceasta " +
                        "operatiune!"));

        UserEntity userWithChangedRoles = userRepository.findById(userToChangeTheRole.getId()).orElse(null);
        assertNotNull(userWithChangedRoles);
        assertEquals(1, userWithChangedRoles.getRoles().size());
        assertFalse(userWithChangedRoles.getRoles().contains(roleFromAnotherOrganization));
    }


    @Test
    @Transactional
     void changeRolesShouldFailWhenAddingRoleToUserFromAnotherOrganization() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                changeRolesShouldFailWhenAddingRoleToUserFromAnotherOrganization(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void changeRolesShouldFailWhenAddingRoleToUserFromAnotherOrganization(UserEntity userEntity) throws Exception {
        OrganizationEntity anotherOrganization = organizationUtils.saveOrganization();
        RoleEntity roleFromAnotherOrganization = roleUtils.saveRole(generateRandomString(), userEntity.getOrganization());
        assertNotNull(roleFromAnotherOrganization);

        UserEntity userFromAnotherOrganization = userUtils.saveUser(generateRandomString(), generateRandomString(),
                anotherOrganization, userEntity.getRoles().stream().findFirst().orElseThrow(() -> new RoleNotFoundException("Rolul nu a fost gasit")));

        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), "123456");
        AssignRoleRequestWrapperVO assignRoleRequestWrapperVO = new AssignRoleRequestWrapperVO();
        assignRoleRequestWrapperVO.setUserId(userFromAnotherOrganization.getId());
        assignRoleRequestWrapperVO.setRoleIds(List.of(roleFromAnotherOrganization.getId())); // Change roles to only "ADMIN"

        mockMvc.perform(MockMvcRequestBuilders.post("/role/changeRoles")
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(assignRoleRequestWrapperVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ups! A aparut o eroare!"))
                .andExpect(jsonPath("$.message").value("Userul cu id-ul "
                        + userFromAnotherOrganization.getId() + " este invalid pentru organizatia " + userEntity.getOrganization().getId()));

        UserEntity userWithChangedRoles = userRepository.findById(userFromAnotherOrganization.getId()).orElse(null);
        assertNotNull(userWithChangedRoles);
        assertEquals(1, userWithChangedRoles.getRoles().size());
        assertFalse(userWithChangedRoles.getRoles().contains(roleFromAnotherOrganization));
    }
}
