package com.edusphere.controllers;

import com.edusphere.controllers.utils.*;
import com.edusphere.entities.OrganizationEntity;
import com.edusphere.entities.PaymentEntity;
import com.edusphere.entities.RoleEntity;
import com.edusphere.entities.UserEntity;
import com.edusphere.exceptions.payments.PaymentNotFoundException;
import com.edusphere.repositories.PaymentRepository;
import com.edusphere.vos.ClassVO;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@AutoConfigureMockMvc
@SpringBootTest(properties = "spring.config.name=application-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = "spring.config.location=classpath:/application-test.properties")
 class PaymentControllerIntegrationTest {
     static final String PASSWORD = "123456";
     static final String PAYMENT_ENDPOINT = "/payment";
    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private OrganizationTestUtils organizationUtils;
    
    @Autowired
    private RoleTestUtils roleUtils;
    
    @Autowired
    private UserTestUtils userUtils;
    
    @Autowired
    private PaymentTestUtils paymentUtils;
    
    @Autowired
    private TokenTestUtils tokenUtils;
    
    @Test
     void markPaymentAsPaid() {
        final List<UserEntity> allowedUsersToCallTheEndpoint = new ArrayList<>();
        OrganizationEntity organizationEntity = organizationUtils.saveOrganization();
        RoleEntity adminRole = roleUtils.saveRole(ADMIN.getName(), organizationEntity);
        RoleEntity ownerRole = roleUtils.saveRole(OWNER.getName(), organizationEntity);
        allowedUsersToCallTheEndpoint.add(userUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, adminRole));
        allowedUsersToCallTheEndpoint.add(userUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, ownerRole));

        try {
            // Test adding an organization when called by different users.
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                markPaymentAsPaid(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void markPaymentAsPaid(UserEntity userEntity) throws Exception {
        PaymentEntity aPayment = paymentUtils.savePayment(userEntity.getOrganization());
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        ClassVO classVO = ClassVO.builder()
                .name(generateRandomString())
                .build();

        // Perform the mockMvc request
        mockMvc.perform(MockMvcRequestBuilders.put(PAYMENT_ENDPOINT + "/markAsPaid/"
                                + aPayment.getId() + "/true")
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(classVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.isPaid").value("true"));

        PaymentEntity PaymentEntity = paymentRepository.findById(aPayment.getId()).orElseThrow(() ->
                new PaymentNotFoundException(aPayment.getId()));

        assertTrue(PaymentEntity.isPaid());
    }

    @Test
     void markPaymentAsUnpaid() {
        final List<UserEntity> allowedUsersToCallTheEndpoint = new ArrayList<>();
        OrganizationEntity organizationEntity = organizationUtils.saveOrganization();
        RoleEntity adminRole = roleUtils.saveRole(ADMIN.getName(), organizationEntity);
        RoleEntity ownerRole = roleUtils.saveRole(OWNER.getName(), organizationEntity);
        allowedUsersToCallTheEndpoint.add(userUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, adminRole));
        allowedUsersToCallTheEndpoint.add(userUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, ownerRole));

        try {
            // Test adding an organization when called by different users.
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                markPaymentAsUnpaid(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void markPaymentAsUnpaid(UserEntity userEntity) throws Exception {
        PaymentEntity aPayment = paymentUtils.saveAPaidPayment(userEntity.getOrganization());
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        ClassVO classVO = ClassVO.builder()
                .name(generateRandomString())
                .build();

        // Perform the mockMvc request
        mockMvc.perform(MockMvcRequestBuilders.put(PAYMENT_ENDPOINT + "/markAsPaid/"
                                + aPayment.getId() + "/false")
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(classVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.isPaid").value("false"));

        PaymentEntity PaymentEntity = paymentRepository.findById(aPayment.getId()).orElseThrow(() ->
                new PaymentNotFoundException(aPayment.getId()));

        assertFalse(PaymentEntity.isPaid());
    }

    @Test
     void markPaymentAsPaid_shouldFailIfAlreadyPaid() {
        final List<UserEntity> allowedUsersToCallTheEndpoint = new ArrayList<>();
        OrganizationEntity organizationEntity = organizationUtils.saveOrganization();
        RoleEntity adminRole = roleUtils.saveRole(ADMIN.getName(), organizationEntity);
        RoleEntity ownerRole = roleUtils.saveRole(OWNER.getName(), organizationEntity);
        allowedUsersToCallTheEndpoint.add(userUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, adminRole));
        allowedUsersToCallTheEndpoint.add(userUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, ownerRole));

        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                markPaymentAsPaid_shouldFailIfAlreadyPaid(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void markPaymentAsPaid_shouldFailIfAlreadyPaid(UserEntity userEntity) throws Exception {
        PaymentEntity aPayment = paymentUtils.saveAPaidPayment(userEntity.getOrganization());
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        ClassVO classVO = ClassVO.builder()
                .name(generateRandomString())
                .build();

        // Perform the mockMvc request
        mockMvc.perform(MockMvcRequestBuilders.put(PAYMENT_ENDPOINT + "/markAsPaid/"
                                + aPayment.getId() + "/true")
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(classVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Factura cu id-ul " + aPayment.getId() + " este deja platita."))
                .andExpect(jsonPath("$.error").value("Ups! A aparut o eroare!"));

        PaymentEntity PaymentEntity = paymentRepository.findById(aPayment.getId()).orElseThrow(() ->
                new PaymentNotFoundException(aPayment.getId()));

        assertTrue(PaymentEntity.isPaid());
    }

    @Test
     void markPayment_shouldFailForNotAllowedRoles() {
        final List<UserEntity> notAllowedUsersToCallTheEndpoint = new ArrayList<>();
        OrganizationEntity organizationEntity = organizationUtils.saveOrganization();
        RoleEntity teacherRole = roleUtils.saveRole(TEACHER.getName(), organizationEntity);
        RoleEntity parentRole = roleUtils.saveRole(PARENT.getName(), organizationEntity);
        notAllowedUsersToCallTheEndpoint.add(userUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, teacherRole));
        notAllowedUsersToCallTheEndpoint.add(userUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, parentRole));

        try {
            for (UserEntity allowedUser : notAllowedUsersToCallTheEndpoint) {
                markPayment_shouldFailForNotAllowedRoles(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void markPayment_shouldFailForNotAllowedRoles(UserEntity userEntity) throws Exception {
        PaymentEntity aPayment = paymentUtils.saveAPaidPayment(userEntity.getOrganization());
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        ClassVO classVO = ClassVO.builder()
                .name(generateRandomString())
                .build();

        // Perform the mockMvc request
        mockMvc.perform(MockMvcRequestBuilders.put(PAYMENT_ENDPOINT + "/markAsPaid/"
                                + aPayment.getId() + "/true")
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(classVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(jsonPath("$.message").value("Nu aveti suficiente drepturi pentru aceasta operatiune!"))
                .andExpect(jsonPath("$.error").value("Access Denied"));

        PaymentEntity PaymentEntity = paymentRepository.findById(aPayment.getId()).orElseThrow(() ->
                new PaymentNotFoundException(aPayment.getId()));

        assertTrue(PaymentEntity.isPaid());
    }

    @Test
     void markPayment_shouldFailForAnotherOrganization() {
        final List<UserEntity> allowedUsersToCallTheEndpoint = new ArrayList<>();
        OrganizationEntity organizationEntity = organizationUtils.saveOrganization();
        RoleEntity adminRole = roleUtils.saveRole(ADMIN.getName(), organizationEntity);
        RoleEntity ownerRole = roleUtils.saveRole(OWNER.getName(), organizationEntity);
        allowedUsersToCallTheEndpoint.add(userUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, adminRole));
        allowedUsersToCallTheEndpoint.add(userUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, ownerRole));

        try {
            // Test adding an organization when called by different users.
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                markPayment_shouldFailForAnotherOrganization(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void markPayment_shouldFailForAnotherOrganization(UserEntity userEntity) throws Exception {
        OrganizationEntity anOrganization = organizationUtils.saveOrganization();
        PaymentEntity aPayment = paymentUtils.savePayment(anOrganization);
        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        ClassVO classVO = ClassVO.builder()
                .name(generateRandomString())
                .build();

        // Perform the mockMvc request
        mockMvc.perform(MockMvcRequestBuilders.put(PAYMENT_ENDPOINT + "/markAsPaid/"
                                + aPayment.getId() + "/true")
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(classVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Factura cu id-ul " + aPayment.getId() + " este invalida."))
                .andExpect(jsonPath("$.error").value("Ups! A aparut o eroare!"));

    }

}
