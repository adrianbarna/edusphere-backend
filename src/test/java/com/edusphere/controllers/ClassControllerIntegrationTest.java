package com.edusphere.controllers;

import com.edusphere.controllers.utils.TestUtils;
import com.edusphere.entities.ClassEntity;
import com.edusphere.entities.OrganizationEntity;
import com.edusphere.entities.RoleEntity;
import com.edusphere.entities.UserEntity;
import com.edusphere.exceptions.ClassNotFoundException;
import com.edusphere.repositories.ClassRepository;
import com.edusphere.vos.ClassVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@AutoConfigureMockMvc
@SpringBootTest(properties = "spring.config.name=application-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = "spring.config.location=classpath:/application-test.properties")
public class ClassControllerIntegrationTest {
    public static final String PASSWORD = "123456";
    public static final String CLASS_ENDPOINT = "/class";
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private ClassRepository classRepository;


    @Test
    public void getAllClasses() {

        final List<UserEntity> allowedUsersToCallTheEndpoint = new ArrayList<>();
        OrganizationEntity organizationEntity = testUtils.saveOrganization();
        RoleEntity adminRole = testUtils.saveRole(ADMIN.getName(), organizationEntity);
        RoleEntity ownerRole = testUtils.saveRole(OWNER.getName(), organizationEntity);
        RoleEntity teacherRole = testUtils.saveRole(TEACHER.getName(), organizationEntity);
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, adminRole));
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, ownerRole));
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, teacherRole));

        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                getAllClasses(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getAllClasses(UserEntity userEntity) throws Exception {
        ClassEntity aClass = testUtils.saveAClassInOrganization(userEntity.getOrganization());
        ClassEntity anotherClass = testUtils.saveAClassInOrganization(userEntity.getOrganization());

        OrganizationEntity anptherOrganization = testUtils.saveOrganization();
        ClassEntity classFromAnotherOrganization = testUtils.saveAClassInOrganization(anptherOrganization);

        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

        mockMvc.perform(MockMvcRequestBuilders.get(CLASS_ENDPOINT)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id", hasItem(aClass.getId())))
                .andExpect(jsonPath("$.[*].id", hasItem(anotherClass.getId())))
                .andExpect(jsonPath("$.[*].id", not(hasItem(classFromAnotherOrganization.getId()))));
    }

    @Test
    public void getAllClasses_shouldFailForNotAllowedRoles() {

        final List<UserEntity> notAllowedUsersToCallTheEndpoint = new ArrayList<>();
        OrganizationEntity organizationEntity = testUtils.saveOrganization();
        RoleEntity parentRole = testUtils.saveRole(PARENT.getName(), organizationEntity);
        notAllowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, parentRole));

        try {
            for (UserEntity allowedUser : notAllowedUsersToCallTheEndpoint) {
                getAllClasses_shouldFailForNotAllowedRoles(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getAllClasses_shouldFailForNotAllowedRoles(UserEntity userEntity) throws Exception {

        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

        mockMvc.perform(MockMvcRequestBuilders.get(CLASS_ENDPOINT)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(jsonPath("$.message").value("Nu aveti suficiente drepturi pentru aceasta operatiune!"))
                .andExpect(jsonPath("$.error").value("Access Denied"));
    }

    @Test
    public void getClassById() {

        final List<UserEntity> allowedUsersToCallTheEndpoint = new ArrayList<>();
        OrganizationEntity organizationEntity = testUtils.saveOrganization();
        RoleEntity adminRole = testUtils.saveRole(ADMIN.getName(), organizationEntity);
        RoleEntity ownerRole = testUtils.saveRole(OWNER.getName(), organizationEntity);
        RoleEntity teacherRole = testUtils.saveRole(TEACHER.getName(), organizationEntity);
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, adminRole));
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, ownerRole));
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, teacherRole));

        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                getClassById(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getClassById(UserEntity userEntity) throws Exception {
        ClassEntity aClass = testUtils.saveAClassInOrganization(userEntity.getOrganization());

        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

        mockMvc.perform(MockMvcRequestBuilders.get(CLASS_ENDPOINT + "/" + aClass.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(aClass.getId()));
    }

    @Test
    public void getClassById_shouldFailWhenIsAWrongOrganization() {

        final List<UserEntity> allowedUsersToCallTheEndpoint = new ArrayList<>();
        OrganizationEntity organizationEntity = testUtils.saveOrganization();
        RoleEntity adminRole = testUtils.saveRole(ADMIN.getName(), organizationEntity);
        RoleEntity ownerRole = testUtils.saveRole(OWNER.getName(), organizationEntity);
        RoleEntity teacherRole = testUtils.saveRole(TEACHER.getName(), organizationEntity);
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, adminRole));
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, ownerRole));
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, teacherRole));

        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                getClassById_shouldFailWhenIsAWrongOrganization(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getClassById_shouldFailWhenIsAWrongOrganization(UserEntity userEntity) throws Exception {
        OrganizationEntity organizationEntity = testUtils.saveOrganization();
        ClassEntity aClass = testUtils.saveAClassInOrganization(organizationEntity);

        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

        mockMvc.perform(MockMvcRequestBuilders.get(CLASS_ENDPOINT + "/" + aClass.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nu exista clasa cu id-ul " + aClass.getId()))
                .andExpect(jsonPath("$.error").value("Ups! A aparut o eroare!"));
    }

    @Test
    public void getClassById_shouldFailForNotAllowedRoles() {

        final List<UserEntity> notAllowedUsersToCallTheEndpoint = new ArrayList<>();
        OrganizationEntity organizationEntity = testUtils.saveOrganization();
        RoleEntity parentRole = testUtils.saveRole(PARENT.getName(), organizationEntity);
        notAllowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, parentRole));

        try {
            for (UserEntity allowedUser : notAllowedUsersToCallTheEndpoint) {
                getClassById_shouldFailForNotAllowedRoles(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getClassById_shouldFailForNotAllowedRoles(UserEntity userEntity) throws Exception {
        ClassEntity aClass = testUtils.saveAClassInOrganization(userEntity.getOrganization());

        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

        mockMvc.perform(MockMvcRequestBuilders.get(CLASS_ENDPOINT + "/" + aClass.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(jsonPath("$.message").value("Nu aveti suficiente drepturi pentru aceasta operatiune!"))
                .andExpect(jsonPath("$.error").value("Access Denied"));
    }


    @Test
    public void addClass() {
        final List<UserEntity> allowedUsersToCallTheEndpoint = new ArrayList<>();
        OrganizationEntity organizationEntity = testUtils.saveOrganization();
        RoleEntity adminRole = testUtils.saveRole(ADMIN.getName(), organizationEntity);
        RoleEntity ownerRole = testUtils.saveRole(OWNER.getName(), organizationEntity);
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, adminRole));
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, ownerRole));

        try {
            // Test adding an organization when called by different users.
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                addClass(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void addClass(UserEntity userEntity) throws Exception {
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        ClassVO classVO = ClassVO.builder()
                .name(generateRandomString())
                .build();

        // Perform the mockMvc request
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/class")
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(classVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value(classVO.getName()))
                .andReturn();

        // Extract the "id" from the response JSON
        String responseContent = result.getResponse().getContentAsString();
        JsonNode jsonNode = new ObjectMapper().readTree(responseContent);
        String id = jsonNode.get("id").asText();

        assertTrue(classRepository.existsById(Integer.valueOf(id)));
    }

    @Test
    public void addClass_shouldFailForWrongRole() {

        final List<UserEntity> notAllowedUsersToCallTheEndpoint = new ArrayList<>();
        OrganizationEntity organizationEntity = testUtils.saveOrganization();
        RoleEntity parentRole = testUtils.saveRole(PARENT.getName(), organizationEntity);
        RoleEntity teacherRole = testUtils.saveRole(TEACHER.getName(), organizationEntity);
        notAllowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, parentRole));
        notAllowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, teacherRole));

        try {
            // Test adding an organization when called by different users.
            for (UserEntity allowedUser : notAllowedUsersToCallTheEndpoint) {
                addClass_shouldFailForWrongRole(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void addClass_shouldFailForWrongRole(UserEntity userEntity) throws Exception {
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        ClassVO classVO = ClassVO.builder()
                .name(generateRandomString())
                .build();

        // Perform the mockMvc request
        mockMvc.perform(MockMvcRequestBuilders.post("/class")
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(classVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(jsonPath("$.message").value("Nu aveti suficiente drepturi pentru aceasta operatiune!"))
                .andExpect(jsonPath("$.error").value("Access Denied"));


        assertFalse(classRepository.findAll().stream()
                .anyMatch(classEntity -> classEntity.getName().equals(classVO.getName())));
    }

    @Test
    public void updateClass() {
        final List<UserEntity> allowedUsersToCallTheEndpoint = new ArrayList<>();
        OrganizationEntity organizationEntity = testUtils.saveOrganization();
        RoleEntity adminRole = testUtils.saveRole(ADMIN.getName(), organizationEntity);
        RoleEntity ownerRole = testUtils.saveRole(OWNER.getName(), organizationEntity);
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, adminRole));
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, ownerRole));

        ClassEntity aClass = testUtils.saveAClassInOrganization(organizationEntity);

        try {
            // Test adding an organization when called by different users.
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                updateClass(allowedUser, aClass.getId());
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void updateClass(UserEntity userEntity, Integer classId) throws Exception {
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        UserEntity aTeacher = testUtils.saveATeacherInOrganization(userEntity.getOrganization());
        ArrayList<Integer> teacherIds = new ArrayList<>();
        teacherIds.add(aTeacher.getId());

        ClassVO classVO = ClassVO.builder()
                .id(classId)
                .name(generateRandomString())
                .teacherIds(teacherIds)
                .build();

        // Perform the mockMvc request
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/class/" + classVO.getId())
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(classVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value(classVO.getName()))
                .andReturn();

        // Extract the "id" from the response JSON
        String responseContent = result.getResponse().getContentAsString();
        JsonNode jsonNode = new ObjectMapper().readTree(responseContent);
        String id = jsonNode.get("id").asText();

        ClassEntity classEntity = classRepository.findById(Integer.valueOf(id))
                .orElseThrow(() -> new ClassNotFoundException(classId));

        assertEquals(classEntity.getName(), classVO.getName());
    }

    @Test
    public void updateClass_shouldFailWhenUpdatingWithTeacherFromAnotherOrganization() {
        final List<UserEntity> allowedUsersToCallTheEndpoint = new ArrayList<>();
        OrganizationEntity organizationEntity = testUtils.saveOrganization();
        RoleEntity adminRole = testUtils.saveRole(ADMIN.getName(), organizationEntity);
        RoleEntity ownerRole = testUtils.saveRole(OWNER.getName(), organizationEntity);
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, adminRole));
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, ownerRole));

        ClassEntity aClass = testUtils.saveAClassInOrganization(organizationEntity);

        try {
            // Test adding an organization when called by different users.
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                updateClass_shouldFailWhenUpdatingWithTeacherFromAnotherOrganization(allowedUser, aClass.getId());
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void updateClass_shouldFailWhenUpdatingWithTeacherFromAnotherOrganization(UserEntity userEntity, Integer classId) throws Exception {
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        OrganizationEntity anOrganization = testUtils.saveOrganization();
        UserEntity aTeacher = testUtils.saveATeacherInOrganization(anOrganization);
        ArrayList<Integer> teacherIds = new ArrayList<>();
        teacherIds.add(aTeacher.getId());

        ClassVO classVO = ClassVO.builder()
                .id(classId)
                .name(generateRandomString())
                .teacherIds(teacherIds)
                .build();

        // Perform the mockMvc request
        mockMvc.perform(MockMvcRequestBuilders.put("/class/" + classVO.getId())
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(classVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ups! A aparut o eroare!"))
                .andExpect(jsonPath("$.message").value("User-ul asignat nu este un profesor!"));
    }

    @Test
    public void updateClass_shouldFailForNotAllowedUsers() {
        final List<UserEntity> notAllowedUsersToCallTheEndpoint = new ArrayList<>();
        OrganizationEntity organizationEntity = testUtils.saveOrganization();
        RoleEntity parentRole = testUtils.saveRole(PARENT.getName(), organizationEntity);
        RoleEntity teacherRole = testUtils.saveRole(TEACHER.getName(), organizationEntity);
        notAllowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, parentRole));
        notAllowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, teacherRole));

        ClassEntity aClass = testUtils.saveAClassInOrganization(organizationEntity);

        try {
            // Test adding an organization when called by different users.
            for (UserEntity allowedUser : notAllowedUsersToCallTheEndpoint) {
                updateClass_shouldFailForNotAllowedUsers(allowedUser, aClass.getId());
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void updateClass_shouldFailForNotAllowedUsers(UserEntity userEntity, Integer classId) throws Exception {
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        UserEntity aTeacher = testUtils.saveATeacherInOrganization(userEntity.getOrganization());
        ArrayList<Integer> teacherIds = new ArrayList<>();
        teacherIds.add(aTeacher.getId());

        ClassVO classVO = ClassVO.builder()
                .id(classId)
                .name(generateRandomString())
                .teacherIds(teacherIds)
                .build();

        // Perform the mockMvc request
        mockMvc.perform(MockMvcRequestBuilders.put("/class/" + classVO.getId())
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(classVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(jsonPath("$.message").value("Nu aveti suficiente drepturi pentru aceasta operatiune!"))
                .andExpect(jsonPath("$.error").value("Access Denied"));
    }

    @Test
    public void deleteClass() {
        final List<UserEntity> allowedUsersToCallTheEndpoint = new ArrayList<>();
        OrganizationEntity organizationEntity = testUtils.saveOrganization();
        RoleEntity adminRole = testUtils.saveRole(ADMIN.getName(), organizationEntity);
        RoleEntity ownerRole = testUtils.saveRole(OWNER.getName(), organizationEntity);
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, adminRole));
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, ownerRole));

        try {
            // Test adding an organization when called by different users.
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                deleteClass(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void deleteClass(UserEntity userEntity) throws Exception {
        ClassEntity aClass = testUtils.saveAClassInOrganization(userEntity.getOrganization());
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

        // Perform the mockMvc request
        mockMvc.perform(MockMvcRequestBuilders.delete("/class/" + aClass.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());


// Assert true if it does not exist
        assertThat(classRepository.findById(aClass.getId()).isEmpty()).isTrue();
    }

    @Test
    public void deleteClass_shouldFailForWrongRole() {
        final List<UserEntity> notAllowedUsersToCallTheEndpoint = new ArrayList<>();
        OrganizationEntity organizationEntity = testUtils.saveOrganization();
        RoleEntity parentRole = testUtils.saveRole(PARENT.getName(), organizationEntity);
        RoleEntity teacherRole = testUtils.saveRole(TEACHER.getName(), organizationEntity);
        notAllowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, parentRole));
        notAllowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, teacherRole));

        try {
            // Test adding an organization when called by different users.
            for (UserEntity allowedUser : notAllowedUsersToCallTheEndpoint) {
                deleteClass_shouldFailForWrongRole(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void deleteClass_shouldFailForWrongRole(UserEntity userEntity) throws Exception {
        ClassEntity aClass = testUtils.saveAClassInOrganization(userEntity.getOrganization());
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

        // Perform the mockMvc request
        mockMvc.perform(MockMvcRequestBuilders.delete("/class/" + aClass.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(jsonPath("$.message").value("Nu aveti suficiente drepturi pentru aceasta operatiune!"))
                .andExpect(jsonPath("$.error").value("Access Denied"));


// Assert true if it does not exist
        assertThat(classRepository.findById(aClass.getId()).isEmpty()).isFalse();
    }

    @Test
    public void deleteClass_shouldFailWhenDeletingClassFromAnotherOrganization() {
        final List<UserEntity> allowedUsersToCallTheEndpoint = new ArrayList<>();
        OrganizationEntity organizationEntity = testUtils.saveOrganization();
        RoleEntity adminRole = testUtils.saveRole(ADMIN.getName(), organizationEntity);
        RoleEntity ownerRole = testUtils.saveRole(OWNER.getName(), organizationEntity);
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, adminRole));
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, ownerRole));

        try {
            // Test adding an organization when called by different users.
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                deleteClass_shouldFailWhenDeletingClassFromAnotherOrganization(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void deleteClass_shouldFailWhenDeletingClassFromAnotherOrganization(UserEntity userEntity) throws Exception {
        OrganizationEntity organizationEntity = testUtils.saveOrganization();
        ClassEntity aClass = testUtils.saveAClassInOrganization(organizationEntity);
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

        // Perform the mockMvc request
        mockMvc.perform(MockMvcRequestBuilders.delete("/class/" + aClass.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ups! A aparut o eroare!"))
                .andExpect(jsonPath("$.message").value("Nu exista clasa cu id-ul " + aClass.getId()));


// Assert true if it does not exist
        assertThat(classRepository.findById(aClass.getId()).isEmpty()).isFalse();
    }

}
