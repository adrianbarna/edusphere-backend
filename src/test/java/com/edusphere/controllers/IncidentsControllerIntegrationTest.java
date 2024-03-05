package com.edusphere.controllers;

import com.edusphere.controllers.utils.TestUtils;
import com.edusphere.entities.*;
import com.edusphere.exceptions.IncidentNotFoundException;
import com.edusphere.repositories.IncidentRepository;
import com.edusphere.vos.IncidentVO;
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

import static com.edusphere.controllers.utils.TestUtils.asJsonString;
import static com.edusphere.controllers.utils.TestUtils.generateRandomString;
import static com.edusphere.enums.RolesEnum.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@AutoConfigureMockMvc
@SpringBootTest(properties = "spring.config.name=application-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = "spring.config.location=classpath:/application-test.properties")
public class IncidentsControllerIntegrationTest {

    public static final String INCIDENTS_ENDPOINT = "/incidents";
    public static final String PASSWORD = "123456";
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private TestUtils testUtils;

    private final List<UserEntity> allowedUsersToCallTheEndpoint = new ArrayList<>();
    private final List<UserEntity> notAllowedUsersToCallTheEndpoint = new ArrayList<>();

    @BeforeAll
    public void setup() {
        OrganizationEntity organizationEntity = testUtils.saveOrganization();
        RoleEntity adminRole = testUtils.saveRole(ADMIN.getName(), organizationEntity);
        RoleEntity ownerRole = testUtils.saveRole(OWNER.getName(), organizationEntity);
        RoleEntity teacherRole = testUtils.saveRole(TEACHER.getName(), organizationEntity);
        RoleEntity parentRole = testUtils.saveRole(PARENT.getName(), organizationEntity);
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), "123456", organizationEntity, adminRole));
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), "123456", organizationEntity, ownerRole));
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), "123456", organizationEntity, teacherRole));
        notAllowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), "123456", organizationEntity, parentRole));
    }

    @Test
    public void getAllIncidents() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                getAllIncidents(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getAllIncidents(UserEntity userEntity) throws Exception {
        String token = testUtils.getTokenForUser(userEntity.getUsername(), "123456");
        IncidentEntity incidentEntity = testUtils.saveIncident(userEntity.getOrganization());
        IncidentEntity anotherIncidentEntity = testUtils.saveIncident(userEntity.getOrganization());

        OrganizationEntity anotherOrganization = testUtils.saveOrganization();
        IncidentEntity incidentInAnotherORganization
                = testUtils.saveIncident(anotherOrganization);


        mockMvc.perform(MockMvcRequestBuilders.get(INCIDENTS_ENDPOINT)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].summary", hasItem(incidentEntity.getSummary())))
                .andExpect(jsonPath("$.[*].summary", hasItem(anotherIncidentEntity.getSummary())))
                .andExpect(jsonPath("$.[*].name", not(hasItem(incidentInAnotherORganization.getSummary()))));
    }

    @Test
    public void getAllIncidents_shouldFailForWrongRole() {
        try {
            for (UserEntity allowedUser : notAllowedUsersToCallTheEndpoint) {
                getAllIncidents_shouldFailForWrongRole(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getAllIncidents_shouldFailForWrongRole(UserEntity userEntity) throws Exception {
        String token = testUtils.getTokenForUser(userEntity.getUsername(), "123456");
        testUtils.saveIncident(userEntity.getOrganization());

        mockMvc.perform(MockMvcRequestBuilders.get(INCIDENTS_ENDPOINT)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(jsonPath("$.message").value("Nu aveti suficiente drepturi pentru aceasta operatiune!"))
                .andExpect(jsonPath("$.error").value("Access Denied"));
    }

    @Test
    public void getIncidentById() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                getIncidentById(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getIncidentById(UserEntity userEntity) throws Exception {
        String token = testUtils.getTokenForUser(userEntity.getUsername(), "123456");
        IncidentEntity incidentEntity = testUtils.saveIncident(userEntity.getOrganization());

        mockMvc.perform(MockMvcRequestBuilders.get(INCIDENTS_ENDPOINT + "/" + incidentEntity.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.summary").value(incidentEntity.getSummary()));
        ;
    }

    @Test
    public void getIncidentById_shoulfFailIfFromAnotherOrganization() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                getIncidentById_shoulfFailIfFromAnotherOrganization(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getIncidentById_shoulfFailIfFromAnotherOrganization(UserEntity userEntity) throws Exception {
        String token = testUtils.getTokenForUser(userEntity.getUsername(), "123456");

        OrganizationEntity anotherOrganization = testUtils.saveOrganization();
        IncidentEntity incidentInAnotherORganization
                = testUtils.saveIncident(anotherOrganization);


        mockMvc.perform(MockMvcRequestBuilders.get(INCIDENTS_ENDPOINT + "/" + incidentInAnotherORganization.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Id-ul incidentului " + incidentInAnotherORganization.getId() + " este invalid"))
                .andExpect(jsonPath("$.error").value("Ups! A aparut o eroare!"));
    }

    @Test
    public void getIncidentById_shoulfFailForWrongRole() {
        try {
            for (UserEntity notAllowedUser : notAllowedUsersToCallTheEndpoint) {
                getIncidentById_shoulfFailForWrongRole(notAllowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getIncidentById_shoulfFailForWrongRole(UserEntity userEntity) throws Exception {
        String token = testUtils.getTokenForUser(userEntity.getUsername(), "123456");
        IncidentEntity incidentEntity = testUtils.saveIncident(userEntity.getOrganization());

        mockMvc.perform(MockMvcRequestBuilders.get(INCIDENTS_ENDPOINT + "/" + incidentEntity.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(jsonPath("$.message").value("Nu aveti suficiente drepturi pentru aceasta operatiune!"))
                .andExpect(jsonPath("$.error").value("Access Denied"));
    }

    @Test
    public void createIncident() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                createIncident(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void createIncident(UserEntity userEntity) throws Exception {
        String token = testUtils.getTokenForUser(userEntity.getUsername(), "123456");
        ChildEntity aChildInOrganization = testUtils.saveAChildInOrganization(userEntity.getOrganization());
        IncidentVO incidentVO = IncidentVO.builder()
                .childId(aChildInOrganization.getId())
                .summary(generateRandomString())
                .isAcknowledged(false)
                .build();

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(INCIDENTS_ENDPOINT)
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(incidentVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.childId").value(incidentVO.getChildId()))
                .andExpect(jsonPath("$.summary").value(incidentVO.getSummary()))
                .andExpect(jsonPath("$.isAcknowledged").value(incidentVO.getIsAcknowledged()))
                .andReturn();

        // Extract the "id" from the response JSON
        String responseContent = result.getResponse().getContentAsString();
        JsonNode jsonNode = new ObjectMapper().readTree(responseContent);
        Integer id = jsonNode.get("id").asInt();

        IncidentEntity createdIncident = incidentRepository.findByIdAndChildParentOrganizationId(id,
                userEntity.getOrganization().getId()).orElseThrow(() -> new IncidentNotFoundException(id));
        assertNotNull(createdIncident);
        assertEquals(incidentVO.getSummary(), createdIncident.getSummary());
        assertEquals(incidentVO.getIsAcknowledged(), createdIncident.getAcknowledged());
    }

    @Test
    public void createIncident_shouldFailForWrongRole() {
        try {
            for (UserEntity notAllowedUser : notAllowedUsersToCallTheEndpoint) {
                createIncident_shouldFailForWrongRole(notAllowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void createIncident_shouldFailForWrongRole(UserEntity userEntity) throws Exception {
        String token = testUtils.getTokenForUser(userEntity.getUsername(), "123456");
        ChildEntity aChildInOrganization = testUtils.saveAChildInOrganization(userEntity.getOrganization());
        IncidentVO incidentVO = IncidentVO.builder()
                .childId(aChildInOrganization.getId())
                .summary(generateRandomString())
                .isAcknowledged(false)
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post(INCIDENTS_ENDPOINT)
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(incidentVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(jsonPath("$.message").value("Nu aveti suficiente drepturi pentru aceasta operatiune!"))
                .andExpect(jsonPath("$.error").value("Access Denied"));
    }

    @Test
    public void updateIncident() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                IncidentEntity incidentEntity = testUtils.saveIncident(allowedUser.getOrganization());
                updateIncident(allowedUser, incidentEntity.getId());
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void updateIncident(UserEntity userEntity, Integer incidentId) throws Exception {
        String token = testUtils.getTokenForUser(userEntity.getUsername(), "123456");
        ChildEntity aChildInOrganization = testUtils.saveAChildInOrganization(userEntity.getOrganization());
        IncidentVO incidentVO = IncidentVO.builder()
                .childId(aChildInOrganization.getId())
                .summary(generateRandomString())
                .isAcknowledged(false)
                .build();

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(INCIDENTS_ENDPOINT + "/" + incidentId)
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(incidentVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.childId").value(incidentVO.getChildId()))
                .andExpect(jsonPath("$.summary").value(incidentVO.getSummary()))
                .andExpect(jsonPath("$.isAcknowledged").value(incidentVO.getIsAcknowledged()))
                .andReturn();

        // Extract the "id" from the response JSON
        String responseContent = result.getResponse().getContentAsString();
        JsonNode jsonNode = new ObjectMapper().readTree(responseContent);
        Integer id = jsonNode.get("id").asInt();

        IncidentEntity createdIncident = incidentRepository.findByIdAndChildParentOrganizationId(id,
                userEntity.getOrganization().getId()).orElseThrow(() -> new IncidentNotFoundException(id));
        assertNotNull(createdIncident);
        assertEquals(incidentVO.getSummary(), createdIncident.getSummary());
        assertEquals(incidentVO.getIsAcknowledged(), createdIncident.getAcknowledged());
    }

    @Test
    public void updateIncident_shouldFailWhenUpdatingFromAnotherOrganization() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                IncidentEntity incidentEntity = testUtils.saveIncident(allowedUser.getOrganization());
                updateIncident_shouldFailWhenUpdatingFromAnotherOrganization(allowedUser, incidentEntity.getId());
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void updateIncident_shouldFailWhenUpdatingFromAnotherOrganization(UserEntity userEntity, Integer incidentId) throws Exception {
        String token = testUtils.getTokenForUser(userEntity.getUsername(), "123456");
        OrganizationEntity anotherOrganization = testUtils.saveOrganization();
        ChildEntity aChildInAnotherOrganization = testUtils.saveAChildInOrganization(anotherOrganization);
        IncidentVO incidentVO = IncidentVO.builder()
                .childId(aChildInAnotherOrganization.getId())
                .summary(generateRandomString())
                .isAcknowledged(false)
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put(INCIDENTS_ENDPOINT + "/" + incidentId)
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(incidentVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Id-ul copilului: "+ aChildInAnotherOrganization.getId()+" este invalid"))
                .andExpect(jsonPath("$.error").value("Ups! A aparut o eroare!"));
    }

    @Test
    public void updateIncident_shouldFailForNotAllowedUsers() {
        try {
            for (UserEntity notAllowedUser : notAllowedUsersToCallTheEndpoint) {
                IncidentEntity incidentEntity = testUtils.saveIncident(notAllowedUser.getOrganization());
                updateIncident_shouldFailForNotAllowedUsers(notAllowedUser, incidentEntity.getId());
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void updateIncident_shouldFailForNotAllowedUsers(UserEntity userEntity, Integer incidentId) throws Exception {
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        ChildEntity aChildInAnotherOrganization = testUtils.saveAChildInOrganization(userEntity.getOrganization());
        IncidentVO incidentVO = IncidentVO.builder()
                .childId(aChildInAnotherOrganization.getId())
                .summary(generateRandomString())
                .isAcknowledged(false)
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put(INCIDENTS_ENDPOINT + "/" + incidentId)
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(incidentVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Nu aveti suficiente drepturi pentru aceasta operatiune!"))
                .andExpect(jsonPath("$.error").value("Access Denied"));
    }

    @Test
    public void deleteIncident() {
        try {
            // Test adding an organization when called by different users.
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                deleteIncident(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void deleteIncident(UserEntity userEntity) throws Exception {
        IncidentEntity incidentEntity = testUtils.saveIncident(userEntity.getOrganization());
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

        // Perform the mockMvc request
        mockMvc.perform(MockMvcRequestBuilders.delete(INCIDENTS_ENDPOINT+"/"+incidentEntity.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());


// Assert true if it does not exist
        assertThat(incidentRepository.findById(incidentEntity.getId()).isEmpty()).isTrue();
    }

    @Test
    public void deleteIncident_shouldFailForWrongRole() {
        try {
            for (UserEntity notAllowedUser : notAllowedUsersToCallTheEndpoint) {
                deleteIncident_shouldFailForWrongRole(notAllowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void deleteIncident_shouldFailForWrongRole(UserEntity userEntity) throws Exception {
        IncidentEntity incidentEntity = testUtils.saveIncident(userEntity.getOrganization());
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

        mockMvc.perform(MockMvcRequestBuilders.delete(INCIDENTS_ENDPOINT+"/"+incidentEntity.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(jsonPath("$.message").value("Nu aveti suficiente drepturi pentru aceasta operatiune!"))
                .andExpect(jsonPath("$.error").value("Access Denied"));;

        assertThat(incidentRepository.findById(incidentEntity.getId()).isEmpty()).isFalse();
    }

    @Test
    public void deleteIncident_shouldFailWhenDeletingFromWrongOrganization() {
        try {
            // Test adding an organization when called by different users.
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                deleteIncident_shouldFailWhenDeletingFromWrongOrganization(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void deleteIncident_shouldFailWhenDeletingFromWrongOrganization(UserEntity userEntity) throws Exception {
        OrganizationEntity anotherOrganization = testUtils.saveOrganization();
        IncidentEntity incidentEntity = testUtils.saveIncident(anotherOrganization);
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

        // Perform the mockMvc request
        mockMvc.perform(MockMvcRequestBuilders.delete(INCIDENTS_ENDPOINT+"/"+incidentEntity.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Id-ul incidentului "+incidentEntity.getId()+" este invalid"))
                .andExpect(jsonPath("$.error").value("Ups! A aparut o eroare!"));;


// Assert true if it does not exist
        assertThat(incidentRepository.findById(incidentEntity.getId()).isEmpty()).isFalse();
    }
}