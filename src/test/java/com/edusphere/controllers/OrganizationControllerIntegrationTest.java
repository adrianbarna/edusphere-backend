package com.edusphere.controllers;

import com.edusphere.controllers.utils.OrganizationTestUtils;
import com.edusphere.controllers.utils.RoleTestUtils;
import com.edusphere.controllers.utils.TokenTestUtils;
import com.edusphere.controllers.utils.UserTestUtils;
import com.edusphere.entities.OrganizationEntity;
import com.edusphere.entities.RoleEntity;
import com.edusphere.entities.UserEntity;
import com.edusphere.repositories.OrganizationRepository;
import com.edusphere.vos.OrganizationVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;

import static com.edusphere.controllers.utils.StringTestUtils.asJsonString;
import static com.edusphere.controllers.utils.StringTestUtils.generateRandomString;
import static com.edusphere.enums.RolesEnum.*;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@AutoConfigureMockMvc
@SpringBootTest(properties = "spring.config.name=application-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = "spring.config.location=classpath:/application-test.properties")
 class OrganizationControllerIntegrationTest {

    static final String PASSWORD = "123456";
    public static final String ORGANIZATIONS_ENDPOINT = "/organizations";
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrganizationRepository organizationRepository;

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
        allowedUsersToCallTheEndpoint.add(userUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, ownerRole));
        notAllowedUsersToCallTheEndpoint.add(userUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, adminRole));
        notAllowedUsersToCallTheEndpoint.add(userUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, teacherRole));
        notAllowedUsersToCallTheEndpoint.add(userUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, parentRole));
    }

    @Test
     void addOrganization() {
        try {
            // Test adding an organization when called by different users.
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                addOrganizationWhenCalledByUser(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }


    private void addOrganizationWhenCalledByUser(UserEntity userEntity) throws Exception {
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        OrganizationVO organizationVO = OrganizationVO.builder()
                .name(generateRandomString())
                .description(generateRandomString())
                .build();

        // Perform the mockMvc request
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(ORGANIZATIONS_ENDPOINT)
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(organizationVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value(organizationVO.getName()))
                .andExpect(jsonPath("$.description").value(organizationVO.getDescription()))
                .andReturn();

        // Extract the "id" from the response JSON
        String responseContent = result.getResponse().getContentAsString();
        JsonNode jsonNode = new ObjectMapper().readTree(responseContent);
        String id = jsonNode.get("id").asText();

        assertTrue(organizationRepository.existsById(Integer.valueOf(id)));
    }

    @Test
     void addOrganizationShouldFailForNotAllowedUser() {
        try {
            // Test adding an organization should fail for not allowed users.
            for (UserEntity notAllowedUser : notAllowedUsersToCallTheEndpoint) {
                addOrganizationWhenCalledByUserShouldFailForNotAllowedUser(notAllowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void addOrganizationWhenCalledByUserShouldFailForNotAllowedUser(UserEntity userEntity) throws Exception {
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        OrganizationVO organizationVO = OrganizationVO.builder()
                .name(generateRandomString())
                .description(generateRandomString())
                .build();

        // Perform the mockMvc request
        mockMvc.perform(MockMvcRequestBuilders.post(ORGANIZATIONS_ENDPOINT)
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(organizationVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
     void getAllOrganizations() {
        try {
            try {
                // Test adding an organization should fail for not allowed users.
                for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                    getAllOrganizationsWhenCalledByUser(allowedUser);
                }
            } catch (Exception e) {
                fail("Test failed due to an exception: " + e.getMessage());
            }


        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getAllOrganizationsWhenCalledByUser(UserEntity allowedUser) throws Exception {
        OrganizationEntity organizationEntity = organizationUtils.saveOrganization();
        String token = tokenUtils.getTokenForUser(allowedUser.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.get(ORGANIZATIONS_ENDPOINT)
                        .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id", hasItem(organizationEntity.getId())))
                .andExpect(jsonPath("$.[*].name", hasItem(organizationEntity.getName())))
                .andExpect(jsonPath("$.[*].description", hasItem(organizationEntity.getDescription())));
    }

    @Test
     void getAllOrganizationsShouldFailForNotAllowedUsers() {
        try {
            try {
                // Test adding an organization should fail for not allowed users.
                for (UserEntity notAllowedUser : notAllowedUsersToCallTheEndpoint) {
                    getAllOrganizationsWhenCalledByUserShouldFailForNotAllowedUser(notAllowedUser);
                }
            } catch (Exception e) {
                fail("Test failed due to an exception: " + e.getMessage());
            }


        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getAllOrganizationsWhenCalledByUserShouldFailForNotAllowedUser(UserEntity allowedUser) throws Exception {
        String token = tokenUtils.getTokenForUser(allowedUser.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.get(ORGANIZATIONS_ENDPOINT)
                        .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
     void getOrganizationById() {
        try {
            // Test adding an organization should fail for not allowed users.
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                getOrganizationByIdWhenCalledByUser(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getOrganizationByIdWhenCalledByUser(UserEntity allowedUser) throws Exception {
        OrganizationEntity organizationEntity = organizationUtils.saveOrganization();
        String token = tokenUtils.getTokenForUser(allowedUser.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.get(ORGANIZATIONS_ENDPOINT + "/{id}", organizationEntity.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(organizationEntity.getId()))
                .andExpect(jsonPath("$.name").value(organizationEntity.getName()))
                .andExpect(jsonPath("$.description").value(organizationEntity.getDescription()));
    }

    @Test
     void getOrganizationByIdShouldFailForNotAllowedUser() {
        try {
            // Test adding an organization should fail for not allowed users.
            for (UserEntity notAllowedUser : notAllowedUsersToCallTheEndpoint) {
                getOrganizationByIdWhenCalledByUserShoudFailForNotAllowedUser(notAllowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getOrganizationByIdWhenCalledByUserShoudFailForNotAllowedUser(UserEntity allowedUser) throws Exception {
        OrganizationEntity organizationEntity = organizationUtils.saveOrganization();
        String token = tokenUtils.getTokenForUser(allowedUser.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.get(ORGANIZATIONS_ENDPOINT + "/{id}", organizationEntity.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
     void getOrganizationByIdShouldFailForInvalidId() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                String token = tokenUtils.getTokenForUser(allowedUser.getUsername(), PASSWORD);
                mockMvc.perform(MockMvcRequestBuilders.get(ORGANIZATIONS_ENDPOINT + "/{id}", -1)
                                .header("Authorization", "Bearer " + token))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    @Test
     void updateOrganization() {
        try {
            // Test updating an organization.
            OrganizationEntity organizationEntity = organizationUtils.saveOrganization();
            String token = tokenUtils.getTokenForUser(allowedUsersToCallTheEndpoint.get(0).getUsername(), PASSWORD);

            OrganizationVO updatedOrganizationVO = OrganizationVO.builder()
                    .name("Updated Org")
                    .description("Updated Description")
                    .build();

            mockMvc.perform(MockMvcRequestBuilders.put(ORGANIZATIONS_ENDPOINT + "/{id}", organizationEntity.getId())
                            .header("Authorization", "Bearer " + token)
                            .content(asJsonString(updatedOrganizationVO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());

            // Verify that the organization was updated
            OrganizationEntity updatedOrganization = organizationRepository.findById(organizationEntity.getId()).orElse(null);
            assertNotNull(updatedOrganization);
            assertEquals(updatedOrganizationVO.getName(), updatedOrganization.getName());
            assertEquals(updatedOrganizationVO.getDescription(), updatedOrganization.getDescription());
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    @Test
     void updateOrganizationShouldFailForNotAllowedUser() {
        try {
            // Test updating an organization should fail for not allowed users.
            OrganizationEntity organizationEntity = organizationUtils.saveOrganization();
            String token = tokenUtils.getTokenForUser(notAllowedUsersToCallTheEndpoint.get(0).getUsername(), PASSWORD);

            OrganizationVO updatedOrganizationVO = OrganizationVO.builder()
                    .name("Updated Org")
                    .description("Updated Description")
                    .build();

            mockMvc.perform(MockMvcRequestBuilders.put(ORGANIZATIONS_ENDPOINT + "/{id}", organizationEntity.getId())
                            .header("Authorization", "Bearer " + token)
                            .content(asJsonString(updatedOrganizationVO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isForbidden());
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    @Test
     void deleteOrganization() {
        try {
            // Test deleting an organization.
            OrganizationEntity organizationEntity = organizationUtils.saveOrganization();
            String token = tokenUtils.getTokenForUser(allowedUsersToCallTheEndpoint.get(0).getUsername(), PASSWORD);

            mockMvc.perform(MockMvcRequestBuilders.delete(ORGANIZATIONS_ENDPOINT + "/{id}", organizationEntity.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(MockMvcResultMatchers.status().isOk());

            // Verify that the organization was deleted
            assertFalse(organizationRepository.existsById(organizationEntity.getId()));
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    @Test
     void deleteOrganizationShouldFailForNotAllowedUser() {
        try {
            // Test deleting an organization should fail for not allowed users.
            OrganizationEntity organizationEntity = organizationUtils.saveOrganization();
            String token = tokenUtils.getTokenForUser(notAllowedUsersToCallTheEndpoint.get(0).getUsername(), PASSWORD);

            mockMvc.perform(MockMvcRequestBuilders.delete(ORGANIZATIONS_ENDPOINT + "/{id}", organizationEntity.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(MockMvcResultMatchers.status().isForbidden());
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }
}
