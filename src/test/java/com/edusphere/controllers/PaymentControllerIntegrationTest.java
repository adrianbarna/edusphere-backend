package com.edusphere.controllers;

import com.edusphere.controllers.utils.TestUtils;
import com.edusphere.entities.*;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.edusphere.controllers.utils.TestUtils.asJsonString;
import static com.edusphere.controllers.utils.TestUtils.generateRandomString;
import static com.edusphere.enums.RolesEnum.*;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@AutoConfigureMockMvc
@SpringBootTest(properties = "spring.config.name=application-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = "spring.config.location=classpath:/application-test.properties")
public class PaymentControllerIntegrationTest {
    public static final String PASSWORD = "123456";
    public static final String PAYMENT_ENDPOINT = "/payment";
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    public void getChildPayments() {

        final List<UserEntity> allowedUsersToCallTheEndpoint = new ArrayList<>();
        OrganizationEntity organizationEntity = testUtils.saveOrganization();
        RoleEntity adminRole = testUtils.saveRole(ADMIN.getName(), organizationEntity);
        RoleEntity ownerRole = testUtils.saveRole(OWNER.getName(), organizationEntity);
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, adminRole));
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, ownerRole));

        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                getChildPayments(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getChildPayments(UserEntity userEntity) throws Exception {
        PaymentEntity paymentEntity = testUtils.savePayment(userEntity.getOrganization());
        PaymentEntity secondPayment = testUtils.savePaymentForChildOnMonth(paymentEntity.getChild(),
                paymentEntity.getIssueDate());
        PaymentEntity paymentForAnotherChild = testUtils.savePayment(userEntity.getOrganization());

        LocalDate currentDate = LocalDate.now();
        LocalDate previousMonth = currentDate.minusMonths(1);

        PaymentEntity paymentEntityFromPreviousMonth = testUtils.savePaymentForChildOnMonth(
                paymentEntity.getChild(), previousMonth);

        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        String formattedDate = currentDate.format(formatter);

        mockMvc.perform(MockMvcRequestBuilders.get(PAYMENT_ENDPOINT + "/child/" + paymentEntity.getChild().getId() +
                                "?month=" + formattedDate)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.[*].id", hasItem(paymentEntity.getId())))
                .andExpect(jsonPath("$.[*].id", hasItem(secondPayment.getId())))
                .andExpect(jsonPath("$.[*].amountWithSkipDays", hasItem(paymentEntity.getAmount())))
                .andExpect(jsonPath("$.[*].id", not(hasItem(paymentEntityFromPreviousMonth.getId()))))
                .andExpect(jsonPath("$.[*].id", not(hasItem(paymentForAnotherChild.getId()))));
    }

    @Test
    public void getChildPayments_shouldFailForNotAllowedRoles() {

        final List<UserEntity> notAllowedUsersToCallTheEndpoint = new ArrayList<>();
        OrganizationEntity organizationEntity = testUtils.saveOrganization();
        RoleEntity parentRole = testUtils.saveRole(PARENT.getName(), organizationEntity);
        notAllowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, parentRole));

        try {
            for (UserEntity allowedUser : notAllowedUsersToCallTheEndpoint) {
                getChildPayments_shouldFailForNotAllowedRoles(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getChildPayments_shouldFailForNotAllowedRoles(UserEntity userEntity) throws Exception {
        PaymentEntity paymentEntity = testUtils.savePayment(userEntity.getOrganization());


        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        LocalDate currentDate = LocalDate.now();
        String formattedDate = currentDate.format(formatter);

        mockMvc.perform(MockMvcRequestBuilders.get(PAYMENT_ENDPOINT + "/child/" + paymentEntity.getChild().getId() +
                                "?month=" + formattedDate)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(jsonPath("$.message").value("Nu aveti suficiente drepturi pentru aceasta operatiune!"))
                .andExpect(jsonPath("$.error").value("Access Denied"));
    }


    @Test
    //TODO cover the cases when child has skipDays
    public void getParentPayments() {

        final List<UserEntity> allowedUsersToCallTheEndpoint = new ArrayList<>();
        OrganizationEntity organizationEntity = testUtils.saveOrganization();
        RoleEntity adminRole = testUtils.saveRole(ADMIN.getName(), organizationEntity);
        RoleEntity ownerRole = testUtils.saveRole(OWNER.getName(), organizationEntity);
        RoleEntity parentRole = testUtils.saveRole(PARENT.getName(), organizationEntity);
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, adminRole));
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, ownerRole));
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, parentRole));

        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                getParentPayments(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getParentPayments(UserEntity userEntity) throws Exception {
        PaymentEntity paymentEntity = testUtils.savePayment(userEntity.getOrganization());
        ChildEntity anotherChild = testUtils.saveAChildWithParentInOrganization(userEntity.getOrganization(), paymentEntity.getChild().getParent());
        PaymentEntity PaymentForSecondChild = testUtils.savePaymentForChildOnMonth(anotherChild, paymentEntity.getIssueDate());

        LocalDate currentDate = LocalDate.now();
        LocalDate previousMonth = currentDate.minusMonths(1);

        PaymentEntity PaymentEntityFromPreviousMonth = testUtils.savePaymentForChildOnMonth(
                paymentEntity.getChild(), previousMonth);

        UserEntity aParent = testUtils.saveAParentInOrganization(userEntity.getOrganization());
        ChildEntity childForAnotherParent = testUtils.saveAChildWithParentInOrganization(userEntity.getOrganization(), aParent);
        PaymentEntity paymentForAnotherParent = testUtils.savePaymentForChildOnMonth(childForAnotherParent, paymentEntity.getIssueDate());

        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        String formattedDate = currentDate.format(formatter);

        mockMvc.perform(MockMvcRequestBuilders.get(PAYMENT_ENDPOINT + "/parent/" + paymentEntity.getChild().getParent().getId() +
                                "?month=" + formattedDate)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.[*].id", hasItem(paymentEntity.getId())))
                .andExpect(jsonPath("$.[*].id", hasItem(PaymentForSecondChild.getId())))
                .andExpect(jsonPath("$.[*].amountWithSkipDays", hasItem(paymentEntity.getAmount())))
                .andExpect(jsonPath("$.[*].amountWithSkipDays", hasItem(PaymentForSecondChild.getAmount())))
                .andExpect(jsonPath("$.[*].id", not(hasItem(PaymentEntityFromPreviousMonth.getId()))))
                .andExpect(jsonPath("$.[*].id", not(hasItem(paymentForAnotherParent.getId()))));
    }

    @Test
    public void getParentPayments_shouldFailForNotAllowedRoles() {

        final List<UserEntity> notAllowedUsersToCallTheEndpoint = new ArrayList<>();
        OrganizationEntity organizationEntity = testUtils.saveOrganization();
        RoleEntity parentRole = testUtils.saveRole(TEACHER.getName(), organizationEntity);
        notAllowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, parentRole));

        try {
            for (UserEntity allowedUser : notAllowedUsersToCallTheEndpoint) {
                getParentPayments_shouldFailForNotAllowedRoles(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getParentPayments_shouldFailForNotAllowedRoles(UserEntity userEntity) throws Exception {
        PaymentEntity paymentEntity = testUtils.savePayment(userEntity.getOrganization());

        LocalDate currentDate = LocalDate.now();

        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        String formattedDate = currentDate.format(formatter);

        mockMvc.perform(MockMvcRequestBuilders.get(PAYMENT_ENDPOINT + "/parent/" + paymentEntity.getChild().getParent().getId() +
                                "?month=" + formattedDate)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(jsonPath("$.message").value("Nu aveti suficiente drepturi pentru aceasta operatiune!"))
                .andExpect(jsonPath("$.error").value("Access Denied"));
    }

    @Test
    public void markPaymentAsPaid() {
        final List<UserEntity> allowedUsersToCallTheEndpoint = new ArrayList<>();
        OrganizationEntity organizationEntity = testUtils.saveOrganization();
        RoleEntity adminRole = testUtils.saveRole(ADMIN.getName(), organizationEntity);
        RoleEntity ownerRole = testUtils.saveRole(OWNER.getName(), organizationEntity);
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, adminRole));
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, ownerRole));

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
        PaymentEntity aPayment = testUtils.savePayment(userEntity.getOrganization());
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        ClassVO classVO = ClassVO.builder()
                .name(generateRandomString())
                .build();

        // Perform the mockMvc request
        mockMvc.perform(MockMvcRequestBuilders.put(PAYMENT_ENDPOINT + "/markAsPaid/"
                                + aPayment.getId())
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(classVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.isPaid").value("true"));

        PaymentEntity PaymentEntity = paymentRepository.findById(aPayment.getId()).orElseThrow(() ->
                new PaymentNotFoundException(aPayment.getId()));

        assertTrue(PaymentEntity.getIsPaid());
    }

    @Test
    public void markPaymentAsPaid_shouldFailIfAlreadyPaid() {
        final List<UserEntity> allowedUsersToCallTheEndpoint = new ArrayList<>();
        OrganizationEntity organizationEntity = testUtils.saveOrganization();
        RoleEntity adminRole = testUtils.saveRole(ADMIN.getName(), organizationEntity);
        RoleEntity ownerRole = testUtils.saveRole(OWNER.getName(), organizationEntity);
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, adminRole));
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, ownerRole));

        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                markPaymentAsPaid_shouldFailIfAlreadyPaid(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void markPaymentAsPaid_shouldFailIfAlreadyPaid(UserEntity userEntity) throws Exception {
        PaymentEntity aPayment = testUtils.saveAPaidPayment(userEntity.getOrganization());
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        ClassVO classVO = ClassVO.builder()
                .name(generateRandomString())
                .build();

        // Perform the mockMvc request
        mockMvc.perform(MockMvcRequestBuilders.put(PAYMENT_ENDPOINT + "/markAsPaid/"
                                + aPayment.getId())
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(classVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Factura cu id-ul " + aPayment.getId() + " este deja platita."))
                .andExpect(jsonPath("$.error").value("Ups! A aparut o eroare!"));

        PaymentEntity PaymentEntity = paymentRepository.findById(aPayment.getId()).orElseThrow(() ->
                new PaymentNotFoundException(aPayment.getId()));

        assertTrue(PaymentEntity.getIsPaid());
    }

    @Test
    public void markPaymentAsPaid_shouldFailForNotAllowedRoles() {
        final List<UserEntity> notAllowedUsersToCallTheEndpoint = new ArrayList<>();
        OrganizationEntity organizationEntity = testUtils.saveOrganization();
        RoleEntity teacherRole = testUtils.saveRole(TEACHER.getName(), organizationEntity);
        RoleEntity parentRole = testUtils.saveRole(PARENT.getName(), organizationEntity);
        notAllowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, teacherRole));
        notAllowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, parentRole));

        try {
            for (UserEntity allowedUser : notAllowedUsersToCallTheEndpoint) {
                markPaymentAsPaid_shouldFailForNotAllowedRoles(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void markPaymentAsPaid_shouldFailForNotAllowedRoles(UserEntity userEntity) throws Exception {
        PaymentEntity aPayment = testUtils.saveAPaidPayment(userEntity.getOrganization());
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        ClassVO classVO = ClassVO.builder()
                .name(generateRandomString())
                .build();

        // Perform the mockMvc request
        mockMvc.perform(MockMvcRequestBuilders.put(PAYMENT_ENDPOINT + "/markAsPaid/"
                                + aPayment.getId())
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(classVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(jsonPath("$.message").value("Nu aveti suficiente drepturi pentru aceasta operatiune!"))
                .andExpect(jsonPath("$.error").value("Access Denied"));

        PaymentEntity PaymentEntity = paymentRepository.findById(aPayment.getId()).orElseThrow(() ->
                new PaymentNotFoundException(aPayment.getId()));

        assertTrue(PaymentEntity.getIsPaid());
    }

    @Test
    public void markPaymentAsPaid_shouldFailForAnotherOrganization() {
        final List<UserEntity> allowedUsersToCallTheEndpoint = new ArrayList<>();
        OrganizationEntity organizationEntity = testUtils.saveOrganization();
        RoleEntity adminRole = testUtils.saveRole(ADMIN.getName(), organizationEntity);
        RoleEntity ownerRole = testUtils.saveRole(OWNER.getName(), organizationEntity);
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, adminRole));
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, ownerRole));

        try {
            // Test adding an organization when called by different users.
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                markPaymentAsPaid_shouldFailForAnotherOrganization(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void markPaymentAsPaid_shouldFailForAnotherOrganization(UserEntity userEntity) throws Exception {
        OrganizationEntity anOrganization = testUtils.saveOrganization();
        PaymentEntity aPayment = testUtils.savePayment(anOrganization);
        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);
        ClassVO classVO = ClassVO.builder()
                .name(generateRandomString())
                .build();

        // Perform the mockMvc request
        mockMvc.perform(MockMvcRequestBuilders.put(PAYMENT_ENDPOINT + "/markAsPaid/"
                                + aPayment.getId())
                        .header("Authorization", "Bearer " + token)
                        .content(asJsonString(classVO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Factura cu id-ul " + aPayment.getId() + " este invalida."))
                .andExpect(jsonPath("$.error").value("Ups! A aparut o eroare!"));

    }

}
