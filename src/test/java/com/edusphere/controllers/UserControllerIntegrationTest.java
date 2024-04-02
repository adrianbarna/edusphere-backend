package com.edusphere.controllers;

import com.edusphere.controllers.exceptions.AssertionFailedError;
import com.edusphere.controllers.utils.OrganizationTestUtils;
import com.edusphere.controllers.utils.RoleTestUtils;
import com.edusphere.controllers.utils.TokenTestUtils;
import com.edusphere.controllers.utils.UserTestUtils;
import com.edusphere.entities.OrganizationEntity;
import com.edusphere.entities.RoleEntity;
import com.edusphere.entities.UserEntity;
import com.edusphere.repositories.UserRepository;
import com.edusphere.vos.UserRequestVO;
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
 class UserControllerIntegrationTest {

    static final String PASSWORD = "123456";
    public static final String USERS_ENDPOINT = "/users";
    @Autowired
    private MockMvc mockMvc;

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
        allowedUsersToCallTheEndpoint.add(userUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, adminRole));
        allowedUsersToCallTheEndpoint.add(userUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, ownerRole));
        notAllowedUsersToCallTheEndpoint.add(userUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, teacherRole));
        notAllowedUsersToCallTheEndpoint.add(userUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, parentRole));
    }


    @Test
     void getAllUsers() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                getAllUsersWhenCalledByUser(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getAllUsersWhenCalledByUser(UserEntity userEntity) throws Exception {
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        RoleEntity teacherRole = roleUtils.saveRole("TEACHER", userEntity.getOrganization());

        UserEntity user2 = userUtils.saveUser(userEntity.getUsername() + "Test", "password2", userEntity.getOrganization(),
                teacherRole);

        UserEntity userFromAnotherOrganization = userUtils.saveAParentInAnotherOrganization();

        mockMvc.perform(MockMvcRequestBuilders.get(USERS_ENDPOINT)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].username", hasItem(userEntity.getUsername())))
                .andExpect(jsonPath("$.[*].username", hasItem(user2.getUsername())))
                .andExpect(jsonPath("$[*].username", not(hasItem(userFromAnotherOrganization.getUsername()))));
    }

    @Test
     void getAllUsersShouldFailForNotAllowedUsers() {
        try {
            for (UserEntity notAllowedUser : notAllowedUsersToCallTheEndpoint) {
                getAllUsersWhenCalledByUserShouldFailForNotAllowedUser(notAllowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getAllUsersWhenCalledByUserShouldFailForNotAllowedUser(UserEntity userEntity) throws Exception {
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

        mockMvc.perform(MockMvcRequestBuilders.get(USERS_ENDPOINT)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
     void getUserById() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                getUserByIdWhenCalledByUser(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getUserByIdWhenCalledByUser(UserEntity userEntity) throws Exception {
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.get(USERS_ENDPOINT + "/" + userEntity.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value(userEntity.getUsername()));
    }

    @Test
     void getUserById_shouldFailForNotAllowedUser() {
        try {
            for (UserEntity notAllowedUser : notAllowedUsersToCallTheEndpoint) {
                getUserByIdWhenCalledByUserShouldFailForNotAllowedUser(notAllowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getUserByIdWhenCalledByUserShouldFailForNotAllowedUser(UserEntity userEntity) throws Exception {
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.get(USERS_ENDPOINT + "/" + userEntity.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
     void getUserByIdFromAnotherOrganizationShouldFail() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                getUserFromAnotherOrganizationByIdShouldFailWhenCalledByUser(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getUserFromAnotherOrganizationByIdShouldFailWhenCalledByUser(UserEntity userEntity) throws Exception {
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        UserEntity aParentFromAnotherOrganzation = userUtils.saveAParentInAnotherOrganization();
        mockMvc.perform(MockMvcRequestBuilders.get(USERS_ENDPOINT + "/" + aParentFromAnotherOrganzation.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Ups! A aparut o eroare!"))
                .andExpect(jsonPath("$.message").value("Id-ul " + aParentFromAnotherOrganzation.getId() + " al user-ului este invalid"));
    }

    @Test
     void createUserTest() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                createUserWhenCalledByUser(allowedUser);
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void createUserWhenCalledByUser(UserEntity userEntity) throws Exception {
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        UserRequestVO userRequestVO = new UserRequestVO();
        userRequestVO.setUsername(userEntity.getUsername() + "Newuser");
        userRequestVO.setPassword("newpassword");
        //a organization which will be rewritten in controller with the authenticated user organization
        userRequestVO.setOrganizationId(100);

        mockMvc.perform(MockMvcRequestBuilders.post(USERS_ENDPOINT)
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(userRequestVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value(userEntity.getUsername() + "Newuser"));
    }

    @Test
     void createUserTestShouldFailForNotAllowedUser() {
        try {
            for (UserEntity notAllowedUser : notAllowedUsersToCallTheEndpoint) {
                createUserWhenCalledByUserShouldFailForNotAllowedUser(notAllowedUser);
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void createUserWhenCalledByUserShouldFailForNotAllowedUser(UserEntity userEntity) throws Exception {
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        UserRequestVO userRequestVO = new UserRequestVO();
        userRequestVO.setUsername(userEntity.getUsername() + "Newuser");
        userRequestVO.setPassword("newpassword");
        //a organization which will be rewritten in controller with the authenticated user organization
        userRequestVO.setOrganizationId(100);

        mockMvc.perform(MockMvcRequestBuilders.post(USERS_ENDPOINT)
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(userRequestVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
     void updateUser() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {

                updateUserWhenCalledByUser(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void updateUserWhenCalledByUser(UserEntity userWhoWantsToDoTheUpdate) throws Exception {
        UserEntity userToBeUpdated = userUtils.saveAParentInOrganization(userWhoWantsToDoTheUpdate.getOrganization());
        String token = tokenUtils.getTokenForUser(userWhoWantsToDoTheUpdate.getUsername(), PASSWORD);

        UserRequestVO userRequestVO = new UserRequestVO();
        userRequestVO.setUsername(generateRandomString());
        userRequestVO.setName("aName");
        userRequestVO.setSurname("aSurname");
        userRequestVO.setIsActivated(true);
        userRequestVO.setPassword("test");

        mockMvc.perform(MockMvcRequestBuilders.put(USERS_ENDPOINT + "/" + userToBeUpdated.getId())
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(userRequestVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value(userRequestVO.getUsername()));

        UserEntity updatedUser = userRepository.findByUsername(userRequestVO.getUsername()).orElseThrow(AssertionFailedError::new);
        assertEquals(userRequestVO.getUsername(), updatedUser.getUsername());
        assertEquals(userRequestVO.getIsActivated(), updatedUser.getIsActivated());
        assertEquals(userRequestVO.getName(), updatedUser.getName());
        assertEquals(userRequestVO.getSurname(), updatedUser.getSurname());
    }

    @Test
     void updateUserShouldFailForNotAllowedUser() {
        try {
            for (UserEntity notAllowedUser : notAllowedUsersToCallTheEndpoint) {

                updateUserWhenCalledByUserShouldFailForNotAllowedUser(notAllowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void updateUserWhenCalledByUserShouldFailForNotAllowedUser(UserEntity userWhoWantsToDoTheUpdate) throws Exception {
        UserEntity userToBeUpdated = userUtils.saveAParentInOrganization(userWhoWantsToDoTheUpdate.getOrganization());
        String token = tokenUtils.getTokenForUser(userWhoWantsToDoTheUpdate.getUsername(), PASSWORD);

        UserRequestVO userRequestVO = new UserRequestVO();
        userRequestVO.setUsername(userWhoWantsToDoTheUpdate.getUsername() + "Updated");
        userRequestVO.setName("aName");
        userRequestVO.setSurname("aSurname");
        userRequestVO.setIsActivated(true);
        userRequestVO.setPassword("test");

        mockMvc.perform(MockMvcRequestBuilders.put(USERS_ENDPOINT + "/" + userToBeUpdated.getId())
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(userRequestVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
     void updateUserFromAnotherOrganizationByAdminShouldFail() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                updateUserWhenCalledByUserFromAnotherOrganizationShouldFaild(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }


    private void updateUserWhenCalledByUserFromAnotherOrganizationShouldFaild(UserEntity userWhoWantsToDoTheUpdate) throws Exception {
        UserEntity aParentFromAnotherOrganization = userUtils.saveAParentInAnotherOrganization();
        String token = tokenUtils.getTokenForUser(userWhoWantsToDoTheUpdate.getUsername(), PASSWORD);

        UserRequestVO userRequestVO = new UserRequestVO();
        userRequestVO.setUsername(generateRandomString());
        userRequestVO.setName(generateRandomString());
        userRequestVO.setSurname(generateRandomString());
        userRequestVO.setIsActivated(true);
        userRequestVO.setPassword(generateRandomString());

        mockMvc.perform(MockMvcRequestBuilders.put(USERS_ENDPOINT + "/" + aParentFromAnotherOrganization.getId())
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(userRequestVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ups! A aparut o eroare!"))
                .andExpect(jsonPath("$.message").value("Userul cu id-ul " + aParentFromAnotherOrganization.getId()
                        + " este invalid pentru organizatia " + userWhoWantsToDoTheUpdate.getOrganization().getId()));


        UserEntity notUpdatedUser = userRepository.findByUsername(aParentFromAnotherOrganization.getUsername()).orElseThrow(AssertionFailedError::new);
        assertNotEquals(userRequestVO.getUsername(), notUpdatedUser.getUsername());
    }

    @Test
     void deleteUser() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                deleteUser(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void deleteUser(UserEntity allowedUser) throws Exception {
        UserEntity aParent = userUtils.saveAParentInOrganization(allowedUser.getOrganization());
        String token = tokenUtils.getTokenForUser(allowedUser.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.delete(USERS_ENDPOINT + "/" + aParent.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        assertFalse(userRepository.existsById(aParent.getId()));
    }

    @Test
     void deleteUserTestShouldFailForNotAllowedUser() {
        try {
            for (UserEntity notAllowedUser : notAllowedUsersToCallTheEndpoint) {
                deleteUserWhenCalledByUserShouldFailForNotAllowedUser(notAllowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void deleteUserWhenCalledByUserShouldFailForNotAllowedUser(UserEntity allowedUser) throws Exception {
        UserEntity aParent = userUtils.saveAParentInOrganization(allowedUser.getOrganization());
        String token = tokenUtils.getTokenForUser(allowedUser.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.delete(USERS_ENDPOINT + "/" + aParent.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
     void deleteUserFromAnotherOrganizationShouldFail() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                deleteUserFromAnotherOrganizationWhenCalledByUserShouldFail(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void deleteUserFromAnotherOrganizationWhenCalledByUserShouldFail(UserEntity allowedUser) throws Exception {
        UserEntity aParentFromAnotherOrganization = userUtils.saveAParentInAnotherOrganization();
        String token = tokenUtils.getTokenForUser(allowedUser.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.delete(USERS_ENDPOINT + "/" + aParentFromAnotherOrganization.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ups! A aparut o eroare!"))
                .andExpect(jsonPath("$.message").value("Id-ul " + aParentFromAnotherOrganization.getId() + " al user-ului este invalid"));

        assertTrue(userRepository.existsById(aParentFromAnotherOrganization.getId()));
    }
}
