package com.edusphere.controllers;

import com.edusphere.controllers.exceptions.AssertionFailedError;
import com.edusphere.controllers.utils.TestUtils;
import com.edusphere.entities.*;
import com.edusphere.repositories.ChildRepository;
import com.edusphere.vos.ChildVO;
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
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@AutoConfigureMockMvc
@SpringBootTest(properties = "spring.config.name=application-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = "spring.config.location=classpath:/application-test.properties")
public class ChildControllerIntegrationTest {

    public static final String PASSWORD = "123456";
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private ChildRepository childRepository;

    private final List<UserEntity> allowedUsersToCallTheEndpoint = new ArrayList<>();
    private final List<UserEntity> notAllowedUsersToCallTheEndpoint = new ArrayList<>();

    @BeforeAll
    public void setup() {
        OrganizationEntity organizationEntity = testUtils.saveOrganization();
        RoleEntity adminRole = testUtils.saveRole(ADMIN.getName(), organizationEntity);
        RoleEntity ownerRole = testUtils.saveRole(OWNER.getName(), organizationEntity);
        RoleEntity teacherRole = testUtils.saveRole(TEACHER.getName(), organizationEntity);
        RoleEntity parentRole = testUtils.saveRole(PARENT.getName(), organizationEntity);
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, adminRole));
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, ownerRole));
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, teacherRole));
        notAllowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, parentRole));
    }


    @Test
    public void getAllChildren() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                getAllChildren(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getAllChildren(UserEntity userEntity) throws Exception {
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

        ChildEntity childEntity = testUtils.saveAChildInOrganization(userEntity.getOrganization());
        ChildEntity childFromAnotherOrganization = testUtils.saveAChildInAnotherOrganization();


        mockMvc.perform(MockMvcRequestBuilders.get("/child")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].name", hasItem(childEntity.getName())))
                .andExpect(jsonPath("$[*].name", not(hasItem(childFromAnotherOrganization.getName()))));
    }

    @Test
    public void getAllChildren_shouldFailForNotAllowedUsers() {
        try {
            for (UserEntity notAllowedUser : notAllowedUsersToCallTheEndpoint) {
                getAllChildren_shouldFailForNotAllowedUsers(notAllowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getAllChildren_shouldFailForNotAllowedUsers(UserEntity userEntity) throws Exception {
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

        mockMvc.perform(MockMvcRequestBuilders.get("/child")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Nu aveti suficiente drepturi pentru aceasta operatiune!"))
                .andExpect(jsonPath("$.error").value("Access Denied"));
    }

    @Test
    public void getChildById() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                getChildById(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getChildById(UserEntity userEntity) throws Exception {
        ChildEntity childEntity = testUtils.saveAChildInOrganization(userEntity.getOrganization());
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.get("/child/{id}", childEntity.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(childEntity.getId()))
                .andExpect(jsonPath("$.name").value(childEntity.getName()))
                .andExpect(jsonPath("$.surname").value(childEntity.getSurname()));
    }

    @Test
    public void getChildById_shouldFailWhenTakenFromAnotherOrganization() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                getChildById_shouldFailWhenTakenFromAnotherOrganization(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getChildById_shouldFailWhenTakenFromAnotherOrganization(UserEntity userEntity) throws Exception {
        OrganizationEntity anotherOrganizationEntity = testUtils.saveOrganization();
        ChildEntity childEntity = testUtils.saveAChildInOrganization(anotherOrganizationEntity);
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.get("/child/{id}", childEntity.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Ups! A aparut o eroare!"))
                .andExpect(jsonPath("$.error")
                        .value("Copilul cu id-ul "+childEntity.getId()+" nu a fost gasit"));
    }

    @Test
    public void getChildById_shouldFailForNotAllowedUser() {
        try {
            for (UserEntity notAllowedUser : notAllowedUsersToCallTheEndpoint) {
                getChildById_shouldFailForNotAllowedUser(notAllowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getChildById_shouldFailForNotAllowedUser(UserEntity userEntity) throws Exception {
        ChildEntity childEntity = testUtils.saveAChildInOrganization(userEntity.getOrganization());
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.get("/child/{id}", childEntity.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value("Nu aveti suficiente drepturi pentru aceasta operatiune!"))
                .andExpect(jsonPath("$.error").value("Access Denied"));
    }

    @Test
    public void getChildByParentId() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                getChildByParentId(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getChildByParentId(UserEntity userEntity) throws Exception {
        ChildEntity childEntity = testUtils.saveAChildInOrganization(userEntity.getOrganization());
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.get("/child/parent/{parentId}", childEntity.getParent().getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(childEntity.getId()))
                .andExpect(jsonPath("$[0].name").value(childEntity.getName()))
                .andExpect(jsonPath("$[0].surname").value(childEntity.getSurname()));
    }

    @Test
    public void getChildByParentId_shouldFailWhenTakenFromAnotherOrganization() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                getChildByParentId_shouldFailWhenTakenFromAnotherOrganization(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getChildByParentId_shouldFailWhenTakenFromAnotherOrganization(UserEntity userEntity) throws Exception {
        OrganizationEntity anotherOrganizationEntity = testUtils.saveOrganization();
        ChildEntity childEntity = testUtils.saveAChildInOrganization(anotherOrganizationEntity);
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.get("/child/parent/{parentId}", childEntity.getParent().getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Ups! A aparut o eroare!"))
                .andExpect(jsonPath("$.error").value("Parintele cu id-ul: " + childEntity.getParent().getId()
                        + " nu are niciun copil in organizatie"));
    }

    @Test
    public void getChildByParentId_shouldFailForNotAllowedUser() {
        try {
            for (UserEntity notAllowedUser : notAllowedUsersToCallTheEndpoint) {
                getChildByParentId_shouldFailForNotAllowedUser(notAllowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getChildByParentId_shouldFailForNotAllowedUser(UserEntity userEntity) throws Exception {
        ChildEntity childEntity = testUtils.saveAChildInOrganization(userEntity.getOrganization());
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.get("/child/parent/{parentId}", childEntity.getParent().getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value("Nu aveti suficiente drepturi pentru aceasta operatiune!"))
                .andExpect(jsonPath("$.error").value("Access Denied"));
    }

    @Test
    public void addChild() {
        try {
            // Test adding an organization when called by different users.
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                addChild(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void addChild(UserEntity userEntity) throws Exception {
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        UserEntity parent = testUtils.saveAParentInOrganization(userEntity.getOrganization());
        ClassEntity aClass = testUtils.saveAClassInOrganization(userEntity.getOrganization());
        ChildVO childVO = ChildVO.builder()
                .name(generateRandomString())
                .surname(generateRandomString())
                .parentId(parent.getId())
                .classId(aClass.getId())
                .build();

        // Perform the mockMvc request
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/child")
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(childVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value(childVO.getName()))
                .andExpect(jsonPath("$.surname").value(childVO.getSurname()))
                .andReturn();

        // Extract the "id" from the response JSON
        String responseContent = result.getResponse().getContentAsString();
        JsonNode jsonNode = new ObjectMapper().readTree(responseContent);
        String id = jsonNode.get("id").asText();

        assertTrue(childRepository.existsById(Integer.valueOf(id)));
    }

    @Test
    public void addChildInAClassFromAnotherOrganization_shouldFail() {
        try {
            // Test adding an organization when called by different users.
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                addChildInAClassFromAnotherOrganization_shouldFail(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void addChildInAClassFromAnotherOrganization_shouldFail(UserEntity userEntity) throws Exception {
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        UserEntity parent = testUtils.saveAParentInOrganization(userEntity.getOrganization());
        OrganizationEntity anotherOrganization = testUtils.saveOrganization();
        ClassEntity aClass = testUtils.saveAClassInOrganization(anotherOrganization);
        ChildVO childVO = ChildVO.builder()
                .name(generateRandomString())
                .surname(generateRandomString())
                .parentId(parent.getId())
                .classId(aClass.getId())
                .build();

        // Perform the mockMvc request
        mockMvc.perform(MockMvcRequestBuilders.post("/child")
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(childVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Ups! A aparut o eroare!"))
                .andExpect(jsonPath("$.error").value("Nu exista clasa cu id-ul "+aClass.getId()));

    }

    @Test
    public void addChildWithParentFromAnotherOrganization_shouldFail() {
        try {
            // Test adding an organization when called by different users.
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                addChildWithParentFromAnotherOrganization_shouldFail(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void addChildWithParentFromAnotherOrganization_shouldFail(UserEntity userEntity) throws Exception {
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        UserEntity parentFromAnotherOrganization = testUtils.saveAParentInAnotherOrganization();
        ClassEntity aClass = testUtils.saveAClassInOrganization(userEntity.getOrganization());
        ChildVO childVO = ChildVO.builder()
                .name(generateRandomString())
                .surname(generateRandomString())
                .parentId(parentFromAnotherOrganization.getId())
                .classId(aClass.getId())
                .build();

        // Perform the mockMvc request
        mockMvc.perform(MockMvcRequestBuilders.post("/child")
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(childVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Ups! A aparut o eroare!"))
                .andExpect(jsonPath("$.error").value("Id-ul "+childVO.getParentId()+" al user-ului este invalid"));

    }

    @Test
    public void updateChild() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {

                updateChild(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void updateChild(UserEntity userEntity) throws Exception {
        ChildEntity childToBeUpdated = testUtils.saveAChildInOrganization(userEntity.getOrganization());
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

        UserEntity aParent = testUtils.saveAParentInOrganization(userEntity.getOrganization());
        ClassEntity aClass = testUtils.saveAClassInOrganization(userEntity.getOrganization());
        ChildVO childVO = ChildVO.builder()
                .name(generateRandomString())
                .surname(generateRandomString())
                .parentId(aParent.getId())
                .classId(aClass.getId())
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put("/child/" + childToBeUpdated.getId())
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(childVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(childVO.getName()))
                .andExpect(jsonPath("$.surname").value(childVO.getSurname()))
                .andExpect(jsonPath("$.parentId").value(childVO.getParentId()))
                .andExpect(jsonPath("$.classId").value(childVO.getClassId()));

        ChildEntity updatedUser = childRepository.findByIdAndParentOrganizationId(childToBeUpdated.getId(),
                userEntity.getOrganization().getId()).orElseThrow(AssertionFailedError::new);
        assertEquals(childVO.getName(), updatedUser.getName());
        assertEquals(childVO.getSurname(), updatedUser.getSurname());
        assertEquals(childVO.getParentId(), updatedUser.getParent().getId());
        assertEquals(childVO.getClassId(), updatedUser.getClassEntity().getId());
    }

    @Test
    public void updateChild_shouldFailWhenParentInAnotherOrganization() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {

                updateChild_shouldFailWhenParentInAnotherOrganization(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void updateChild_shouldFailWhenParentInAnotherOrganization(UserEntity userEntity) throws Exception {
        ChildEntity childToBeUpdated = testUtils.saveAChildInOrganization(userEntity.getOrganization());
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

        UserEntity parentFromAnotherOrganization = testUtils.saveAParentInAnotherOrganization();
        ClassEntity aClass = testUtils.saveAClassInOrganization(userEntity.getOrganization());
        ChildVO childVO = ChildVO.builder()
                .name(generateRandomString())
                .surname(generateRandomString())
                .parentId(parentFromAnotherOrganization.getId())
                .classId(aClass.getId())
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put("/child/" + childToBeUpdated.getId())
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(childVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Ups! A aparut o eroare!"))
                .andExpect(jsonPath("$.error").value("Id-ul parintelui este invalid: "+childVO.getParentId()));


        ChildEntity updatedUser = childRepository.findByIdAndParentOrganizationId(childToBeUpdated.getId(),
                userEntity.getOrganization().getId()).orElseThrow(AssertionFailedError::new);
        assertNotEquals(childVO.getName(), updatedUser.getName());
        assertNotEquals(childVO.getSurname(), updatedUser.getSurname());
        assertNotEquals(childVO.getParentId(), updatedUser.getParent().getId());
        assertNotEquals(childVO.getClassId(), updatedUser.getClassEntity().getId());
    }

    @Test
    public void updateChild_shouldFailWhenClassInAnotherOrganization() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {

                updateChild_shouldFailWhenClassInAnotherOrganization(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void updateChild_shouldFailWhenClassInAnotherOrganization(UserEntity userEntity) throws Exception {
        ChildEntity childToBeUpdated = testUtils.saveAChildInOrganization(userEntity.getOrganization());
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

        UserEntity aParent = testUtils.saveAParentInOrganization(userEntity.getOrganization());
        ClassEntity aClass = testUtils.saveAClassInAnotherOrganization();
        ChildVO childVO = ChildVO.builder()
                .name(generateRandomString())
                .surname(generateRandomString())
                .parentId(aParent.getId())
                .classId(aClass.getId())
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put("/child/" + childToBeUpdated.getId())
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(childVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Ups! A aparut o eroare!"))
                .andExpect(jsonPath("$.error").value("Nu exista clasa cu id-ul "+childVO.getClassId()));


        ChildEntity updatedUser = childRepository.findByIdAndParentOrganizationId(childToBeUpdated.getId(),
                userEntity.getOrganization().getId()).orElseThrow(AssertionFailedError::new);
        assertNotEquals(childVO.getName(), updatedUser.getName());
        assertNotEquals(childVO.getSurname(), updatedUser.getSurname());
        assertNotEquals(childVO.getParentId(), updatedUser.getParent().getId());
        assertNotEquals(childVO.getClassId(), updatedUser.getClassEntity().getId());
    }

    @Test
    public void updateChild_shouldFailForNotAllowedUsers() {
        try {
            for (UserEntity notAllowedUser : notAllowedUsersToCallTheEndpoint) {

                updateChild_shouldFailForNotAllowedUsers(notAllowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void updateChild_shouldFailForNotAllowedUsers(UserEntity userEntity) throws Exception {
        ChildEntity childToBeUpdated = testUtils.saveAChildInOrganization(userEntity.getOrganization());
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

        UserEntity aParent = testUtils.saveAParentInOrganization(userEntity.getOrganization());
        ClassEntity aClass = testUtils.saveAClassInAnotherOrganization();
        ChildVO childVO = ChildVO.builder()
                .name(generateRandomString())
                .surname(generateRandomString())
                .parentId(aParent.getId())
                .classId(aClass.getId())
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put("/child/" + childToBeUpdated.getId())
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(childVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value("Nu aveti suficiente drepturi pentru aceasta operatiune!"))
                .andExpect(jsonPath("$.error").value("Access Denied"));


        ChildEntity updatedUser = childRepository.findByIdAndParentOrganizationId(childToBeUpdated.getId(),
                userEntity.getOrganization().getId()).orElseThrow(AssertionFailedError::new);
        assertNotEquals(childVO.getName(), updatedUser.getName());
        assertNotEquals(childVO.getSurname(), updatedUser.getSurname());
        assertNotEquals(childVO.getParentId(), updatedUser.getParent().getId());
        assertNotEquals(childVO.getClassId(), updatedUser.getClassEntity().getId());
    }

    @Test
    public void deleteChild() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                deleteChild(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void deleteChild(UserEntity userEntity) throws Exception {
        ChildEntity aChild = testUtils.saveAChildInOrganization(userEntity.getOrganization());
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.delete("/child/" + aChild.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        assertFalse(childRepository.existsById(aChild.getId()));
    }

    @Test
    public void deleteChild_shouldFailWhenChildInAnotherOrganization() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                deleteChild_shouldFailWhenChildInAnotherOrganization(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void deleteChild_shouldFailWhenChildInAnotherOrganization(UserEntity userEntity) throws Exception {
        ChildEntity aChild = testUtils.saveAChildInAnotherOrganization();
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.delete("/child/" + aChild.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        assertTrue(childRepository.existsById(aChild.getId()));
    }

    @Test
    public void deleteChild_shouldFailForNotAllowedUsers() {
        try {
            for (UserEntity notAllowedUser : notAllowedUsersToCallTheEndpoint) {
                deleteChild_shouldFailForNotAllowedUsers(notAllowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void deleteChild_shouldFailForNotAllowedUsers(UserEntity userEntity) throws Exception {
        ChildEntity aChild = testUtils.saveAChildInAnotherOrganization();
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.delete("/child/" + aChild.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden());

        assertTrue(childRepository.existsById(aChild.getId()));
    }
}
