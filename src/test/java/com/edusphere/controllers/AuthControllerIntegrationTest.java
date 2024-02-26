package com.edusphere.controllers;

import com.edusphere.controllers.exceptions.AssertionFailedError;
import com.edusphere.controllers.utils.TestUtils;
import com.edusphere.entities.OrganizationEntity;
import com.edusphere.entities.RoleEntity;
import com.edusphere.entities.UserEntity;
import com.edusphere.repositories.UserRepository;
import com.edusphere.vos.LoginRequestVO;
import com.edusphere.vos.UserRequestVO;
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
public class AuthControllerIntegrationTest {

    public static final String PASSWORD = "123456";
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestUtils testUtils;



    @Test
    public void authenticateUser() throws Exception {
        OrganizationEntity organizationEntity = testUtils.saveOrganization(generateRandomString(), generateRandomString());
        RoleEntity roleEntity = testUtils.saveRole(OWNER.toString(), organizationEntity);
        UserEntity userEntity = testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, roleEntity);

        LoginRequestVO loginRequestVO = new LoginRequestVO();
        loginRequestVO.setUsername(userEntity.getUsername());
        loginRequestVO.setPassword(PASSWORD);

        MvcResult result =  mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .content(asJsonString(loginRequestVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String token = result.getResponse().getContentAsString();

        //do another call to verify that the token is valid
        mockMvc.perform(MockMvcRequestBuilders.get("/user")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }
}
