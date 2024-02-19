package com.edusphere.controllers;

import com.edusphere.controllers.utils.TestUtils;
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
import org.springframework.test.web.servlet.MockMvc;
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
public class UserControllerIntegrationTest {

    public static final String PASSWORD = "123456";
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestUtils testUtils;

    private final List<UserEntity> allowedUsersToCallTheEndpoint = new ArrayList<>();
    private final List<UserEntity> notAllowedUsersToCallTheEndpoint = new ArrayList<>();

    @BeforeAll
    public void setup() {
        OrganizationEntity organizationEntity = testUtils.saveOrganization(generateRandomString(), "aDescription");
        RoleEntity adminRole = testUtils.saveRole(ADMIN.getName(), organizationEntity);
        RoleEntity ownerRole = testUtils.saveRole(OWNER.getName(), organizationEntity);
        RoleEntity teacherRole = testUtils.saveRole(TEACHER.getName(), organizationEntity);
        RoleEntity parentRole = testUtils.saveRole(PARENT.getName(), organizationEntity);
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, adminRole));
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, ownerRole));
        notAllowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, teacherRole));
        notAllowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, parentRole));
    }


    @Test
    public void getAllUsers() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                getAllUsersWhenCalledByUser(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getAllUsersWhenCalledByUser(UserEntity userEntity) throws Exception {
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        RoleEntity teacherRole = testUtils.saveRole("TEACHER", userEntity.getOrganization());

        UserEntity user2 = testUtils.saveUser(userEntity.getUsername() + "Test", "password2", userEntity.getOrganization(),
                teacherRole);

        UserEntity userFromAnotherOrganization = testUtils.saveAParentInAnotherOrganization();

        mockMvc.perform(MockMvcRequestBuilders.get("/user")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].username", hasItem(userEntity.getUsername())))
                .andExpect(jsonPath("$.[*].username", hasItem(user2.getUsername())))
                .andExpect(jsonPath("$[*].username", not(hasItem(userFromAnotherOrganization.getUsername()))));
    }

    @Test
    public void getAllUsersShouldFailForNotAllowedUsers() {
        try {
            for (UserEntity notAllowedUser : notAllowedUsersToCallTheEndpoint) {
                getAllUsersWhenCalledByUserShouldFailForNotAllowedUser(notAllowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getAllUsersWhenCalledByUserShouldFailForNotAllowedUser(UserEntity userEntity) throws Exception {
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

        mockMvc.perform(MockMvcRequestBuilders.get("/user")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void getUserById() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                getUserByIdWhenCalledByUser(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getUserByIdWhenCalledByUser(UserEntity userEntity) throws Exception {
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.get("/user/" + userEntity.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value(userEntity.getUsername()));
    }

    @Test
    public void getUserById_shouldFailForNotAllowedUser() {
        try {
            for (UserEntity notAllowedUser : notAllowedUsersToCallTheEndpoint) {
                getUserByIdWhenCalledByUserShouldFailForNotAllowedUser(notAllowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getUserByIdWhenCalledByUserShouldFailForNotAllowedUser(UserEntity userEntity) throws Exception {
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.get("/user/" + userEntity.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void getUserByIdFromAnotherOrganizationShouldFail() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                getUserFromAnotherOrganizationByIdShouldFailWhenCalledByUser(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getUserFromAnotherOrganizationByIdShouldFailWhenCalledByUser(UserEntity userEntity) throws Exception {
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        UserEntity aParentFromAnotherOrganzation = testUtils.saveAParentInAnotherOrganization();
        mockMvc.perform(MockMvcRequestBuilders.get("/user/" + aParentFromAnotherOrganzation.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Ups! A aparut o eroare!"))
                .andExpect(jsonPath("$.error").value("Id-ul " + aParentFromAnotherOrganzation.getId() + " al user-ului este invalid"));
    }

    @Test
    public void createUserTest() {
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
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        UserRequestVO userRequestVO = new UserRequestVO();
        userRequestVO.setUsername(userEntity.getUsername() + "Newuser");
        userRequestVO.setPassword("newpassword");
        //a organization which will be rewritten in controller with the authenticated user organization
        userRequestVO.setOrganizationId(100);

        mockMvc.perform(MockMvcRequestBuilders.post("/user")
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(userRequestVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value(userEntity.getUsername() + "Newuser"));
    }

    @Test
    public void createUserTestShouldFailForNotAllowedUser() {
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
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        UserRequestVO userRequestVO = new UserRequestVO();
        userRequestVO.setUsername(userEntity.getUsername() + "Newuser");
        userRequestVO.setPassword("newpassword");
        //a organization which will be rewritten in controller with the authenticated user organization
        userRequestVO.setOrganizationId(100);

        mockMvc.perform(MockMvcRequestBuilders.post("/user")
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(userRequestVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void updateUser() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {

                updateUserWhenCalledByUser(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void updateUserWhenCalledByUser(UserEntity userWhoWantsToDoTheUpdate) throws Exception {
        UserEntity userToBeUpdated = testUtils.saveAParentInOrganization(userWhoWantsToDoTheUpdate.getOrganization());
        String token = testUtils.getTokenForUser(userWhoWantsToDoTheUpdate.getUsername(), PASSWORD);

        UserRequestVO userRequestVO = new UserRequestVO();
        userRequestVO.setUsername(userWhoWantsToDoTheUpdate.getUsername() + "Updated");
        userRequestVO.setName("aName");
        userRequestVO.setSurname("aSurname");
        userRequestVO.setIsActivated(true);
        userRequestVO.setPassword("test");

        mockMvc.perform(MockMvcRequestBuilders.put("/user/" + userToBeUpdated.getId())
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(userRequestVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value(userWhoWantsToDoTheUpdate.getUsername() + "Updated"));

        UserEntity updatedUser = userRepository.findByUsername(userRequestVO.getUsername()).get();
        assertEquals(userRequestVO.getUsername(), updatedUser.getUsername());
        assertEquals(userRequestVO.getIsActivated(), updatedUser.getActivated());
        assertEquals(userRequestVO.getName(), updatedUser.getName());
        assertEquals(userRequestVO.getSurname(), updatedUser.getSurname());
    }

    @Test
    public void updateUserShouldFailForNotAllowedUser() {
        try {
            for (UserEntity notAllowedUser : notAllowedUsersToCallTheEndpoint) {

                updateUserWhenCalledByUserShouldFailForNotAllowedUser(notAllowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void updateUserWhenCalledByUserShouldFailForNotAllowedUser(UserEntity userWhoWantsToDoTheUpdate) throws Exception {
        UserEntity userToBeUpdated = testUtils.saveAParentInOrganization(userWhoWantsToDoTheUpdate.getOrganization());
        String token = testUtils.getTokenForUser(userWhoWantsToDoTheUpdate.getUsername(), PASSWORD);

        UserRequestVO userRequestVO = new UserRequestVO();
        userRequestVO.setUsername(userWhoWantsToDoTheUpdate.getUsername() + "Updated");
        userRequestVO.setName("aName");
        userRequestVO.setSurname("aSurname");
        userRequestVO.setIsActivated(true);
        userRequestVO.setPassword("test");

        mockMvc.perform(MockMvcRequestBuilders.put("/user/" + userToBeUpdated.getId())
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(userRequestVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void updateUserFromAnotherOrganizationByAdminShouldFail() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                updateUserWhenCalledByUserFromAnotherOrganizationShouldFaild(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }


    private void updateUserWhenCalledByUserFromAnotherOrganizationShouldFaild(UserEntity userWhoWantsToDoTheUpdate) throws Exception {
        UserEntity aParentFromAnotherOrganization = testUtils.saveAParentInAnotherOrganization();
        String token = testUtils.getTokenForUser(userWhoWantsToDoTheUpdate.getUsername(), PASSWORD);

        UserRequestVO userRequestVO = new UserRequestVO();
        userRequestVO.setUsername(generateRandomString());
        userRequestVO.setName(generateRandomString());
        userRequestVO.setSurname(generateRandomString());
        userRequestVO.setIsActivated(true);
        userRequestVO.setPassword(generateRandomString());

        mockMvc.perform(MockMvcRequestBuilders.put("/user/" + aParentFromAnotherOrganization.getId())
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(userRequestVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ups! A aparut o eroare!"))
                .andExpect(jsonPath("$.error").value("Userul cu id-ul " + aParentFromAnotherOrganization.getId()
                        + " este invalid pentru organizatia " + userWhoWantsToDoTheUpdate.getOrganization().getId()));


        UserEntity notUpdatedUser = userRepository.findByUsername(aParentFromAnotherOrganization.getUsername()).get();
        assertNotEquals(userRequestVO.getUsername(), notUpdatedUser.getUsername());
    }

    @Test
    public void deleteUserTest() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                deleteUserWhenCalledByUser(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void deleteUserWhenCalledByUser(UserEntity allowedUser) throws Exception {
        UserEntity aParent = testUtils.saveAParentInOrganization(allowedUser.getOrganization());
        String token = testUtils.getTokenForUser(allowedUser.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.delete("/user/" + aParent.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        assertFalse(userRepository.existsById(aParent.getId()));
    }

    @Test
    public void deleteUserTestShouldFailForNotAllowedUser() {
        try {
            for (UserEntity notAllowedUser : notAllowedUsersToCallTheEndpoint) {
                deleteUserWhenCalledByUserShouldFailForNotAllowedUser(notAllowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void deleteUserWhenCalledByUserShouldFailForNotAllowedUser(UserEntity allowedUser) throws Exception {
        UserEntity aParent = testUtils.saveAParentInOrganization(allowedUser.getOrganization());
        String token = testUtils.getTokenForUser(allowedUser.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.delete("/user/" + aParent.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void deleteUserFromAnotherOrganizationShouldFail() {
        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                deleteUserFromAnotherOrganizationWhenCalledByUserShouldFail(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void deleteUserFromAnotherOrganizationWhenCalledByUserShouldFail(UserEntity allowedUser) throws Exception {
        UserEntity aParentFromAnotherOrganization = testUtils.saveAParentInAnotherOrganization();
        String token = testUtils.getTokenForUser(allowedUser.getUsername(), PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.delete("/user/" + aParentFromAnotherOrganization.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ups! A aparut o eroare!"))
                .andExpect(jsonPath("$.error").value("Id-ul " + aParentFromAnotherOrganization.getId() + " al user-ului este invalid"));

        assertTrue(userRepository.existsById(aParentFromAnotherOrganization.getId()));
    }
}
