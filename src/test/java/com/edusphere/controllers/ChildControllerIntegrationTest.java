package com.edusphere.controllers;

import com.edusphere.controllers.utils.TestUtils;
import com.edusphere.entities.ChildEntity;
import com.edusphere.entities.OrganizationEntity;
import com.edusphere.entities.RoleEntity;
import com.edusphere.entities.UserEntity;
import com.edusphere.repositories.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;

import static com.edusphere.controllers.utils.TestUtils.generateRandomString;
import static com.edusphere.enums.RolesEnum.*;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@AutoConfigureMockMvc
@SpringBootTest(properties = "spring.config.name=application-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//TODO add all tests for child controller
public class ChildControllerIntegrationTest {

    public static final String PASSWORD = "123456";
    @Autowired
    private MockMvc mockMvc;

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
}
