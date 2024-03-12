package com.edusphere.controllers;

import com.edusphere.controllers.utils.TestUtils;
import com.edusphere.entities.*;
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

import static com.edusphere.controllers.utils.TestUtils.generateRandomString;
import static com.edusphere.enums.RolesEnum.*;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@AutoConfigureMockMvc
@SpringBootTest(properties = "spring.config.name=application-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = "spring.config.location=classpath:/application-test.properties")
public class InvoiceControllerIntegrationTest {
    public static final String PASSWORD = "123456";
    public static final String INVOICE_ENDPOINT = "/invoice";
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestUtils testUtils;

    @Test
    public void getChildInvoices() {

        final List<UserEntity> allowedUsersToCallTheEndpoint = new ArrayList<>();
        OrganizationEntity organizationEntity = testUtils.saveOrganization();
        RoleEntity adminRole = testUtils.saveRole(ADMIN.getName(), organizationEntity);
        RoleEntity ownerRole = testUtils.saveRole(OWNER.getName(), organizationEntity);
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, adminRole));
        allowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, ownerRole));

        try {
            for (UserEntity allowedUser : allowedUsersToCallTheEndpoint) {
                getChildInvoices(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getChildInvoices(UserEntity userEntity) throws Exception {
        InvoiceEntity invoiceEntity = testUtils.saveInvoice(userEntity.getOrganization());
        InvoiceEntity secondInvoice = testUtils.saveInvoiceForChildOnMonth(invoiceEntity.getChild(),
                invoiceEntity.getIssueDate());
        InvoiceEntity invoiceForAnotherChild = testUtils.saveInvoice(userEntity.getOrganization());

        LocalDate currentDate = LocalDate.now();
        LocalDate previousMonth = currentDate.minusMonths(1);

        InvoiceEntity invoiceEntityFromPreviousMonth = testUtils.saveInvoiceForChildOnMonth(
                invoiceEntity.getChild(), previousMonth);

        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        String formattedDate = currentDate.format(formatter);

        mockMvc.perform(MockMvcRequestBuilders.get(INVOICE_ENDPOINT + "/child/" + invoiceEntity.getChild().getId() +
                                "?month=" + formattedDate)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.[*].id", hasItem(invoiceEntity.getId())))
                .andExpect(jsonPath("$.[*].id", hasItem(secondInvoice.getId())))
                .andExpect(jsonPath("$.[*].amount", hasItem(invoiceEntity.getAmount())))
                .andExpect(jsonPath("$.[*].id", not(hasItem(invoiceEntityFromPreviousMonth.getId()))))
                .andExpect(jsonPath("$.[*].id", not(hasItem(invoiceForAnotherChild.getId()))));
    }

    @Test
    public void getChildInvoices_shouldFailForNotAllowedRoles() {

        final List<UserEntity> notAllowedUsersToCallTheEndpoint = new ArrayList<>();
        OrganizationEntity organizationEntity = testUtils.saveOrganization();
        RoleEntity parentRole = testUtils.saveRole(PARENT.getName(), organizationEntity);
        notAllowedUsersToCallTheEndpoint.add(testUtils.saveUser(generateRandomString(), PASSWORD, organizationEntity, parentRole));

        try {
            for (UserEntity allowedUser : notAllowedUsersToCallTheEndpoint) {
                getChildInvoices_shouldFailForNotAllowedRoles(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getChildInvoices_shouldFailForNotAllowedRoles(UserEntity userEntity) throws Exception {
        InvoiceEntity invoiceEntity = testUtils.saveInvoice(userEntity.getOrganization());


        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        LocalDate currentDate = LocalDate.now();
        String formattedDate = currentDate.format(formatter);

        mockMvc.perform(MockMvcRequestBuilders.get(INVOICE_ENDPOINT + "/child/" + invoiceEntity.getChild().getId() +
                                "?month=" + formattedDate)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(jsonPath("$.message").value("Nu aveti suficiente drepturi pentru aceasta operatiune!"))
                .andExpect(jsonPath("$.error").value("Access Denied"));
    }


    @Test
    public void getParentInvoices() {

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
                getParentInvoices(allowedUser);
            }
        } catch (Exception e) {
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    private void getParentInvoices(UserEntity userEntity) throws Exception {
        InvoiceEntity invoiceEntity = testUtils.saveInvoice(userEntity.getOrganization());
        ChildEntity anotherChild = testUtils.saveAChildWithParentInOrganization(userEntity.getOrganization(), invoiceEntity.getChild().getParent());
        InvoiceEntity invoiceForSecondChild = testUtils.saveInvoiceForChildOnMonth(anotherChild, invoiceEntity.getIssueDate());

        LocalDate currentDate = LocalDate.now();
        LocalDate previousMonth = currentDate.minusMonths(1);

        InvoiceEntity invoiceEntityFromPreviousMonth = testUtils.saveInvoiceForChildOnMonth(
                invoiceEntity.getChild(), previousMonth);

        UserEntity aParent = testUtils.saveAParentInOrganization(userEntity.getOrganization());
        ChildEntity childForAnotherParent = testUtils.saveAChildWithParentInOrganization(userEntity.getOrganization(), aParent);
        InvoiceEntity invoiceForAnotherParent = testUtils.saveInvoiceForChildOnMonth(childForAnotherParent, invoiceEntity.getIssueDate());

        String token = testUtils.getTokenForUser(userEntity.getUsername(), PASSWORD);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        String formattedDate = currentDate.format(formatter);

        mockMvc.perform(MockMvcRequestBuilders.get(INVOICE_ENDPOINT + "/parent/" + invoiceEntity.getChild().getParent().getId() +
                                "?month=" + formattedDate)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.[*].id", hasItem(invoiceEntity.getId())))
                .andExpect(jsonPath("$.[*].id", hasItem(invoiceForSecondChild.getId())))
                .andExpect(jsonPath("$.[*].amount", hasItem(invoiceEntity.getAmount())))
                .andExpect(jsonPath("$.[*].amount", hasItem(invoiceForSecondChild.getAmount())))
                .andExpect(jsonPath("$.[*].id", not(hasItem(invoiceEntityFromPreviousMonth.getId()))))
                .andExpect(jsonPath("$.[*].id", not(hasItem(invoiceForAnotherParent.getId()))));
    }
}
