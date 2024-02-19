package com.edusphere.controllers;

import com.edusphere.controllers.utils.TestUtils;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;

import static com.edusphere.controllers.utils.TestUtils.asJsonString;
import static com.edusphere.controllers.utils.TestUtils.generateRandomString;
import static com.edusphere.enums.RolesEnum.*;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@AutoConfigureMockMvc
@SpringBootTest(properties = "spring.config.name=application-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = "spring.config.location=classpath:/application-test.properties")
public class OrganizationControllerIntegrationTest {

    public static final String PASSWORD = "123456";
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private OrganizationRepository organizationRepository;

    private final List<UserEntity> allowedUsersToCallTheEndpoint = new ArrayList<>();
    private final List<UserEntity> notAllowedUsersToCallTheEndpoint = new ArrayList<>();

    @BeforeAll
    public void setup() {
        OrganizationEntity organizationEntity = testUtils.saveOrganization(generateRandomString(), "aDescription");
        RoleEntity adminRole = testUtils.saveRole(ADMIN.getName(), organizationEntity);
        RoleEntity ownerRole = testUtils.saveRole(OWNER.getName(), organizationEntity);
        RoleEntity teacherRole = testUtils.saveRole(TEACHER.getName(), organizationEntity);
        RoleEntity parentRole = testUtils.saveRole(PARENT.getName(), organizationEntity);
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, ownerRole));
        notAllowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, adminRole));
        notAllowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, teacherRole));
        notAllowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, parentRole));
    }

    @Test
    public void addOrganization() {
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
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        OrganizationVO organizationVO = OrganizationVO.builder()
                .name(generateRandomString())
                .description(generateRandomString())
                .build();

        // Perform the mockMvc request
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/organization")
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(organizationVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
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
    public void addOrganizationShouldFailForNotAllowedUser() {
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
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        OrganizationVO organizationVO = OrganizationVO.builder()
                .name(generateRandomString())
                .description(generateRandomString())
                .build();

        // Perform the mockMvc request
        mockMvc.perform(MockMvcRequestBuilders.post("/organization")
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(organizationVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void getAllOrganizations() {
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
        OrganizationEntity organizationEntity = testUtils.saveOrganization(generateRandomString(), generateRandomString());
        String token = testUtils.getTokenForUser(allowedUser.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.get("/organization")
                .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id", hasItem(organizationEntity.getId())))
                .andExpect(jsonPath("$.[*].name", hasItem(organizationEntity.getName())))
                .andExpect(jsonPath("$.[*].description", hasItem(organizationEntity.getDescription())));
    }

    @Test
    public void getAllOrganizationsShouldFailForNotAllowedUsers() {
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
        String token = testUtils.getTokenForUser(allowedUser.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.get("/organization")
                        .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void getOrganizationById() {
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
        OrganizationEntity organizationEntity = testUtils.saveOrganization(generateRandomString(), generateRandomString());
        String token = testUtils.getTokenForUser(allowedUser.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.get("/organization/{id}", organizationEntity.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(organizationEntity.getId()))
                .andExpect(jsonPath("$.name").value(organizationEntity.getName()))
                .andExpect(jsonPath("$.description").value(organizationEntity.getDescription()));
    }

    @Test
    public void getOrganizationByIdShouldFailForNotAllowedUser() {
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
        OrganizationEntity organizationEntity = testUtils.saveOrganization(generateRandomString(), "Test Description");
        String token = testUtils.getTokenForUser(allowedUser.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.get("/organization/{id}", organizationEntity.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void getOrganizationByIdShouldFailForInvalidId() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                String token = testUtils.getTokenForUser(allowedUser.getUsername(), PASSWORD);
                mockMvc.perform(MockMvcRequestBuilders.get("/organization/{id}", -1)
                        .header("Authorization", "Bearer " + token))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    @Test
    public void updateOrganization() {
        try {
            // Test updating an organization.
            OrganizationEntity organizationEntity = testUtils.saveOrganization(generateRandomString(), "Test Description");
            String token = testUtils.getTokenForUser(allowedUsersToCallTheEndpoint.get(0).getUsername(), PASSWORD);

            OrganizationVO updatedOrganizationVO = OrganizationVO.builder()
                    .name("Updated Org")
                    .description("Updated Description")
                    .build();

            mockMvc.perform(MockMvcRequestBuilders.put("/organization/{id}", organizationEntity.getId())
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
    public void updateOrganizationShouldFailForNotAllowedUser() {
        try {
            // Test updating an organization should fail for not allowed users.
            OrganizationEntity organizationEntity = testUtils.saveOrganization(generateRandomString(), "Test Description");
            String token = testUtils.getTokenForUser(notAllowedUsersToCallTheEndpoint.get(0).getUsername(), PASSWORD);

            OrganizationVO updatedOrganizationVO = OrganizationVO.builder()
                    .name("Updated Org")
                    .description("Updated Description")
                    .build();

            mockMvc.perform(MockMvcRequestBuilders.put("/organization/{id}", organizationEntity.getId())
                            .header("Authorization", "Bearer " + token)
                            .content(asJsonString(updatedOrganizationVO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isForbidden());
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    @Test
    public void deleteOrganization() {
        try {
            // Test deleting an organization.
            OrganizationEntity organizationEntity = testUtils.saveOrganization(generateRandomString(), "Test Description");
            String token = testUtils.getTokenForUser(allowedUsersToCallTheEndpoint.get(0).getUsername(), PASSWORD);

            mockMvc.perform(MockMvcRequestBuilders.delete("/organization/{id}", organizationEntity.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(MockMvcResultMatchers.status().isOk());

            // Verify that the organization was deleted
            assertFalse(organizationRepository.existsById(organizationEntity.getId()));
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    @Test
    public void deleteOrganizationShouldFailForNotAllowedUser() {
        try {
            // Test deleting an organization should fail for not allowed users.
            OrganizationEntity organizationEntity = testUtils.saveOrganization("TestOrg", "Test Description");
            String token = testUtils.getTokenForUser(notAllowedUsersToCallTheEndpoint.get(0).getUsername(), PASSWORD);

            mockMvc.perform(MockMvcRequestBuilders.delete("/organization/{id}", organizationEntity.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(MockMvcResultMatchers.status().isForbidden());
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }
}
