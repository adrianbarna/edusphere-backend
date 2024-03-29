package com.edusphere.controllers;

import com.edusphere.controllers.exceptions.AssertionFailedError;
import com.edusphere.controllers.utils.*;
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

import static com.edusphere.controllers.utils.StringTestUtils.asJsonString;
import static com.edusphere.controllers.utils.StringTestUtils.generateRandomString;
import static com.edusphere.enums.RolesEnum.*;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@AutoConfigureMockMvc
@SpringBootTest(properties = "spring.config.name=application-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = "spring.config.location=classpath:/application-test.properties")
 class ChildControllerIntegrationTest {

     static final String PASSWORD = "123456";
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private OrganizationTestUtils organizationUtils;

    @Autowired
    private RoleTestUtils roleUtils;

    @Autowired
    private TokenTestUtils tokenUtils;

    @Autowired
    private ChildTestUtils childUtils;

    @Autowired
    private ClassTestUtils classUtils;

    @Autowired
    private UserTestUtils userUtils;
    
    private final List<UserEntity> allowedUsersToCallTheEndpoint = new ArrayList<>();
    private final List<UserEntity> notAllowedUsersToCallTheEndpoint = new ArrayList<>();

    @BeforeAll
     void setup() {
        OrganizationEntity organizationEntity = organizationUtils.saveOrganization();
        RoleEntity adminRole = roleUtils.saveRole(ADMIN.getName(), organizationEntity);
        RoleEntity ownerRole = roleUtils.saveRole(OWNER.getName(), organizationEntity);
        RoleEntity teacherRole = roleUtils.saveRole(TEACHER.getName(), organizationEntity);
        RoleEntity parentRole = roleUtils.saveRole(PARENT.getName(), organizationEntity);
        allowedUsersToCallTheEndpoint.add(userUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, adminRole));
        allowedUsersToCallTheEndpoint.add(userUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, ownerRole));
        allowedUsersToCallTheEndpoint.add(userUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, teacherRole));
        notAllowedUsersToCallTheEndpoint.add(userUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, parentRole));
    }


    @Test
     void getAllChildren() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                getAllChildren(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getAllChildren(UserEntity userEntity) throws Exception {
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

        ChildEntity childEntity = childUtils.saveAChildInOrganization(userEntity.getOrganization());
        ChildEntity childFromAnotherOrganization = childUtils.saveAChildInAnotherOrganization();


        mockMvc.perform(MockMvcRequestBuilders.get("/child")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].name", hasItem(childEntity.getName())))
                .andExpect(jsonPath("$[*].name", not(hasItem(childFromAnotherOrganization.getName()))));
    }

    @Test
     void getAllChildren_shouldFailForNotAllowedUsers() {
        try {
            for (UserEntity notAllowedUser : notAllowedUsersToCallTheEndpoint) {
                getAllChildren_shouldFailForNotAllowedUsers(notAllowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getAllChildren_shouldFailForNotAllowedUsers(UserEntity userEntity) throws Exception {
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

        mockMvc.perform(MockMvcRequestBuilders.get("/child")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Nu aveti suficiente drepturi pentru aceasta operatiune!"))
                .andExpect(jsonPath("$.error").value("Access Denied"));
    }

    @Test
     void getChildById() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                getChildById(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getChildById(UserEntity userEntity) throws Exception {
        ChildEntity childEntity = childUtils.saveAChildInOrganization(userEntity.getOrganization());
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.get("/child/{id}", childEntity.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(childEntity.getId()))
                .andExpect(jsonPath("$.name").value(childEntity.getName()))
                .andExpect(jsonPath("$.surname").value(childEntity.getSurname()));
    }

    @Test
     void getChildById_shouldFailWhenTakenFromAnotherOrganization() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                getChildById_shouldFailWhenTakenFromAnotherOrganization(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getChildById_shouldFailWhenTakenFromAnotherOrganization(UserEntity userEntity) throws Exception {
        OrganizationEntity anotherOrganizationEntity = organizationUtils.saveOrganization();
        ChildEntity childEntity = childUtils.saveAChildInOrganization(anotherOrganizationEntity);
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.get("/child/{id}", childEntity.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Ups! A aparut o eroare!"))
                .andExpect(jsonPath("$.message")
                        .value("Copilul cu id-ul " + childEntity.getId() + " nu a fost gasit"));
    }

    @Test
     void getChildById_shouldFailForNotAllowedUser() {
        try {
            for (UserEntity notAllowedUser : notAllowedUsersToCallTheEndpoint) {
                getChildById_shouldFailForNotAllowedUser(notAllowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getChildById_shouldFailForNotAllowedUser(UserEntity userEntity) throws Exception {
        ChildEntity childEntity = childUtils.saveAChildInOrganization(userEntity.getOrganization());
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.get("/child/{id}", childEntity.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value("Nu aveti suficiente drepturi pentru aceasta operatiune!"))
                .andExpect(jsonPath("$.error").value("Access Denied"));
    }

    @Test
     void getChildByParentId() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                getChildByParentId(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getChildByParentId(UserEntity userEntity) throws Exception {
        ChildEntity childEntity = childUtils.saveAChildInOrganization(userEntity.getOrganization());
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.get("/child/parent/{parentId}", childEntity.getParent().getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(childEntity.getId()))
                .andExpect(jsonPath("$[0].name").value(childEntity.getName()))
                .andExpect(jsonPath("$[0].surname").value(childEntity.getSurname()));
    }

    @Test
     void getChildByParentId_shouldFailWhenTakenFromAnotherOrganization() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                getChildByParentId_shouldFailWhenTakenFromAnotherOrganization(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getChildByParentId_shouldFailWhenTakenFromAnotherOrganization(UserEntity userEntity) throws Exception {
        OrganizationEntity anotherOrganizationEntity = organizationUtils.saveOrganization();
        ChildEntity childEntity = childUtils.saveAChildInOrganization(anotherOrganizationEntity);
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.get("/child/parent/{parentId}", childEntity.getParent().getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Ups! A aparut o eroare!"))
                .andExpect(jsonPath("$.message").value("Parintele cu id-ul: " + childEntity.getParent().getId()
                        + " nu are niciun copil in organizatie"));
    }

    @Test
     void getChildByParentId_shouldFailForNotAllowedUser() {
        try {
            for (UserEntity notAllowedUser : notAllowedUsersToCallTheEndpoint) {
                getChildByParentId_shouldFailForNotAllowedUser(notAllowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getChildByParentId_shouldFailForNotAllowedUser(UserEntity userEntity) throws Exception {
        ChildEntity childEntity = childUtils.saveAChildInOrganization(userEntity.getOrganization());
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.get("/child/parent/{parentId}", childEntity.getParent().getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value("Nu aveti suficiente drepturi pentru aceasta operatiune!"))
                .andExpect(jsonPath("$.error").value("Access Denied"));
    }

    @Test
     void getChildForParent() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                getChildForParent(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getChildForParent(UserEntity userEntity) throws Exception {
        ChildEntity childEntity = childUtils.saveAChildInOrganization(userEntity.getOrganization());
        String token = tokenUtils.getTokenForUser(childEntity.getParent().getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.get("/child/forParent")
                        .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(childEntity.getId()))
                .andExpect(jsonPath("$[0].name").value(childEntity.getName()))
                .andExpect(jsonPath("$[0].surname").value(childEntity.getSurname()));
    }

    @Test
     void getChildForParen_shouldFailWhenRetrievingWrongChild() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                getChildForParen_shouldFailWhenRetrievingWrongChild(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getChildForParen_shouldFailWhenRetrievingWrongChild(UserEntity userEntity) throws Exception {
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.get("/child/forParent")
                        .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Ups! A aparut o eroare!"))
                .andExpect(jsonPath("$.message").value("Parintele cu id-ul: " + userEntity.getId() + " nu are niciun copil in organizatie"));
    }

    @Test
     void addChild() {
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
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        UserEntity parent = userUtils.saveAParentInOrganization(userEntity.getOrganization());
        ClassEntity aClass = classUtils.saveAClassInOrganization(userEntity.getOrganization());
        ChildVO childVO = ChildVO.builder()
                .name(generateRandomString())
                .surname(generateRandomString())
                .parentId(parent.getId())
                .classId(aClass.getId())
                .baseTax(2000)
                .build();

        // Perform the mockMvc request
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/child")
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(childVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated())
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
     void addChildInAClassFromAnotherOrganization_shouldFail() {
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
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        UserEntity parent = userUtils.saveAParentInOrganization(userEntity.getOrganization());
        OrganizationEntity anotherOrganization = organizationUtils.saveOrganization();
        ClassEntity aClass = classUtils.saveAClassInOrganization(anotherOrganization);
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
                .andExpect(jsonPath("$.error")
                        .value("Ups! A aparut o eroare!"))
                .andExpect(jsonPath("$.message").value("Nu exista clasa cu id-ul " + aClass.getId()));

    }

    @Test
     void addChildWithParentFromAnotherOrganization_shouldFail() {
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
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        UserEntity parentFromAnotherOrganization = userUtils.saveAParentInAnotherOrganization();
        ClassEntity aClass = classUtils.saveAClassInOrganization(userEntity.getOrganization());
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
                .andExpect(jsonPath("$.error")
                        .value("Ups! A aparut o eroare!"))
                .andExpect(jsonPath("$.message").value("Id-ul " + childVO.getParentId() + " al user-ului este invalid"));

    }

    @Test
     void updateChild() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {

                updateChild(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void updateChild(UserEntity userEntity) throws Exception {
        ChildEntity childToBeUpdated = childUtils.saveAChildInOrganization(userEntity.getOrganization());
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

        UserEntity aParent = userUtils.saveAParentInOrganization(userEntity.getOrganization());
        ClassEntity aClass = classUtils.saveAClassInOrganization(userEntity.getOrganization());
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
     void updateChild_shouldFailWhenParentInAnotherOrganization() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {

                updateChild_shouldFailWhenParentInAnotherOrganization(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void updateChild_shouldFailWhenParentInAnotherOrganization(UserEntity userEntity) throws Exception {
        ChildEntity childToBeUpdated = childUtils.saveAChildInOrganization(userEntity.getOrganization());
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

        UserEntity parentFromAnotherOrganization = userUtils.saveAParentInAnotherOrganization();
        ClassEntity aClass = classUtils.saveAClassInOrganization(userEntity.getOrganization());
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
                .andExpect(jsonPath("$.error")
                        .value("Ups! A aparut o eroare!"))
                .andExpect(jsonPath("$.message").value("Id-ul parintelui este invalid: " + childVO.getParentId()));


        ChildEntity updatedUser = childRepository.findByIdAndParentOrganizationId(childToBeUpdated.getId(),
                userEntity.getOrganization().getId()).orElseThrow(AssertionFailedError::new);
        assertNotEquals(childVO.getName(), updatedUser.getName());
        assertNotEquals(childVO.getSurname(), updatedUser.getSurname());
        assertNotEquals(childVO.getParentId(), updatedUser.getParent().getId());
        assertNotEquals(childVO.getClassId(), updatedUser.getClassEntity().getId());
    }

    @Test
     void updateChild_shouldFailWhenClassInAnotherOrganization() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {

                updateChild_shouldFailWhenClassInAnotherOrganization(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void updateChild_shouldFailWhenClassInAnotherOrganization(UserEntity userEntity) throws Exception {
        ChildEntity childToBeUpdated = childUtils.saveAChildInOrganization(userEntity.getOrganization());
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

        UserEntity aParent = userUtils.saveAParentInOrganization(userEntity.getOrganization());
        ClassEntity aClass = classUtils.saveAClassInAnotherOrganization();
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
                .andExpect(jsonPath("$.error")
                        .value("Ups! A aparut o eroare!"))
                .andExpect(jsonPath("$.message").value("Nu exista clasa cu id-ul " + childVO.getClassId()));


        ChildEntity updatedUser = childRepository.findByIdAndParentOrganizationId(childToBeUpdated.getId(),
                userEntity.getOrganization().getId()).orElseThrow(AssertionFailedError::new);
        assertNotEquals(childVO.getName(), updatedUser.getName());
        assertNotEquals(childVO.getSurname(), updatedUser.getSurname());
        assertNotEquals(childVO.getParentId(), updatedUser.getParent().getId());
        assertNotEquals(childVO.getClassId(), updatedUser.getClassEntity().getId());
    }

    @Test
     void updateChild_shouldFailForNotAllowedUsers() {
        try {
            for (UserEntity notAllowedUser : notAllowedUsersToCallTheEndpoint) {

                updateChild_shouldFailForNotAllowedUsers(notAllowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void updateChild_shouldFailForNotAllowedUsers(UserEntity userEntity) throws Exception {
        ChildEntity childToBeUpdated = childUtils.saveAChildInOrganization(userEntity.getOrganization());
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

        UserEntity aParent = userUtils.saveAParentInOrganization(userEntity.getOrganization());
        ClassEntity aClass = classUtils.saveAClassInAnotherOrganization();
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
     void deleteChild() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                deleteChild(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void deleteChild(UserEntity userEntity) throws Exception {
        ChildEntity aChild = childUtils.saveAChildInOrganization(userEntity.getOrganization());
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.delete("/child/" + aChild.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        assertFalse(childRepository.existsById(aChild.getId()));
    }

    @Test
     void deleteChild_shouldFailWhenChildInAnotherOrganization() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                deleteChild_shouldFailWhenChildInAnotherOrganization(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void deleteChild_shouldFailWhenChildInAnotherOrganization(UserEntity userEntity) throws Exception {
        ChildEntity aChild = childUtils.saveAChildInAnotherOrganization();
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.delete("/child/" + aChild.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        assertTrue(childRepository.existsById(aChild.getId()));
    }

    @Test
     void deleteChild_shouldFailForNotAllowedUsers() {
        try {
            for (UserEntity notAllowedUser : notAllowedUsersToCallTheEndpoint) {
                deleteChild_shouldFailForNotAllowedUsers(notAllowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void deleteChild_shouldFailForNotAllowedUsers(UserEntity userEntity) throws Exception {
        ChildEntity aChild = childUtils.saveAChildInAnotherOrganization();
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.delete("/child/" + aChild.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden());

        assertTrue(childRepository.existsById(aChild.getId()));
    }
}
