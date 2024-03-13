package com.edusphere.controllers;

import com.edusphere.controllers.utils.*;
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
public class PaymentControllerIntegrationTest {
    public static final String PASSWORD = "123456";
    public static final String PAYMENT_ENDPOINT = "/payment";
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
    
    @Autowired
    private ChildTestUtils childUtils;

    @Test
    public void getChildPayments() {

        final List<UserEntity> allowedUsersToCallTheEndpoint = new ArrayList<>();
        OrganizationEntity organizationEntity = organizationUtils.saveOrganization();
        RoleEntity adminRole = roleUtils.saveRole(ADMIN.getName(), organizationEntity);
        RoleEntity ownerRole = roleUtils.saveRole(OWNER.getName(), organizationEntity);
        allowedUsersToCallTheEndpoint.add(userUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, adminRole));
        allowedUsersToCallTheEndpoint.add(userUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, ownerRole));

        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                getChildPayments(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getChildPayments(UserEntity userEntity) throws Exception {
        PaymentEntity paymentEntity = paymentUtils.savePayment(userEntity.getOrganization());
        PaymentEntity secondPayment = paymentUtils.savePaymentForChildOnMonth(paymentEntity.getChild(),
                paymentEntity.getIssueDate());
        PaymentEntity paymentForAnotherChild = paymentUtils.savePayment(userEntity.getOrganization());

        LocalDate currentDate = LocalDate.now();
        LocalDate previousMonth = currentDate.minusMonths(1);

        PaymentEntity paymentEntityFromPreviousMonth = paymentUtils.savePaymentForChildOnMonth(
                paymentEntity.getChild(), previousMonth);

        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

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
        OrganizationEntity organizationEntity = organizationUtils.saveOrganization();
        RoleEntity parentRole = roleUtils.saveRole(PARENT.getName(), organizationEntity);
        notAllowedUsersToCallTheEndpoint.add(userUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, parentRole));

        try {
            for (UserEntity allowedUser : notAllowedUsersToCallTheEndpoint) {
                getChildPayments_shouldFailForNotAllowedRoles(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getChildPayments_shouldFailForNotAllowedRoles(UserEntity userEntity) throws Exception {
        PaymentEntity paymentEntity = paymentUtils.savePayment(userEntity.getOrganization());


        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

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
        OrganizationEntity organizationEntity = organizationUtils.saveOrganization();
        RoleEntity adminRole = roleUtils.saveRole(ADMIN.getName(), organizationEntity);
        RoleEntity ownerRole = roleUtils.saveRole(OWNER.getName(), organizationEntity);
        RoleEntity parentRole = roleUtils.saveRole(PARENT.getName(), organizationEntity);
        allowedUsersToCallTheEndpoint.add(userUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, adminRole));
        allowedUsersToCallTheEndpoint.add(userUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, ownerRole));
        allowedUsersToCallTheEndpoint.add(userUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, parentRole));

        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                getParentPayments(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getParentPayments(UserEntity userEntity) throws Exception {
        PaymentEntity paymentEntity = paymentUtils.savePayment(userEntity.getOrganization());
        ChildEntity anotherChild = childUtils.saveAChildWithParentInOrganization(userEntity.getOrganization(), paymentEntity.getChild().getParent());
        PaymentEntity PaymentForSecondChild = paymentUtils.savePaymentForChildOnMonth(anotherChild, paymentEntity.getIssueDate());

        LocalDate currentDate = LocalDate.now();
        LocalDate previousMonth = currentDate.minusMonths(1);

        PaymentEntity PaymentEntityFromPreviousMonth = paymentUtils.savePaymentForChildOnMonth(
                paymentEntity.getChild(), previousMonth);

        UserEntity aParent = userUtils.saveAParentInOrganization(userEntity.getOrganization());
        ChildEntity childForAnotherParent = childUtils.saveAChildWithParentInOrganization(userEntity.getOrganization(), aParent);
        PaymentEntity paymentForAnotherParent = paymentUtils.savePaymentForChildOnMonth(childForAnotherParent, paymentEntity.getIssueDate());

        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

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
        OrganizationEntity organizationEntity = organizationUtils.saveOrganization();
        RoleEntity parentRole = roleUtils.saveRole(TEACHER.getName(), organizationEntity);
        notAllowedUsersToCallTheEndpoint.add(userUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, parentRole));

        try {
            for (UserEntity allowedUser : notAllowedUsersToCallTheEndpoint) {
                getParentPayments_shouldFailForNotAllowedRoles(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getParentPayments_shouldFailForNotAllowedRoles(UserEntity userEntity) throws Exception {
        PaymentEntity paymentEntity = paymentUtils.savePayment(userEntity.getOrganization());

        LocalDate currentDate = LocalDate.now();

        String token = tokenUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

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

        assertTrue(PaymentEntity.getIsPaid());
    }

    @Test
    public void markPaymentAsUnpaid() {
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

        assertFalse(PaymentEntity.getIsPaid());
    }

    @Test
    public void markPaymentAsPaid_shouldFailIfAlreadyPaid() {
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

        assertTrue(PaymentEntity.getIsPaid());
    }

    @Test
    public void markPayment_shouldFailForNotAllowedRoles() {
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

        assertTrue(PaymentEntity.getIsPaid());
    }

    @Test
    public void markPayment_shouldFailForAnotherOrganization() {
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
