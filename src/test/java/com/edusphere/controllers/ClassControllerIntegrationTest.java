package com.edusphere.controllers;

import com.edusphere.controllers.utils.TestUtils;
import com.edusphere.entities.OrganizationEntity;
import com.edusphere.entities.RoleEntity;
import com.edusphere.entities.UserEntity;
import com.edusphere.repositories.ChildRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static com.edusphere.controllers.utils.TestUtils.generateRandomString;
import static com.edusphere.enums.RolesEnum.*;

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

//    @Test
//    public void getAllClasses() {
//        OrganizationEntity organizationEntity = testUtils.saveOrganization();
//        testUtils.saveAClassInOrganization(organizationEntity);
//        try {
//            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
//                getAllClasses(allowedUser);
//            }
//        } catch (Exception e) {
//            fail("Test failed due to an exception: " + e.getMessage());
//        }
//    }

//    private void getAllClasses(UserEntity userEntity) throws Exception {
//        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
//
//        mockMvc.perform(MockMvcRequestBuilders.get(CLASS_ENDPOINT)
//                        .header("Authorization", "Bearer " + token)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.[*].name", hasItem(childEntity.getName())))
//                .andExpect(jsonPath("$[*].name", not(hasItem(childFromAnotherOrganization.getName()))));
//    }

}