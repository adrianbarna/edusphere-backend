package com.edusphere.controllers.utils;

import com.edusphere.controllers.AuthController;
import com.edusphere.entities.*;
import com.edusphere.enums.PaymentTypeEnum;
import com.edusphere.repositories.*;
import com.edusphere.vos.LoginRequestVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDate;

import static com.edusphere.enums.RolesEnum.PARENT;
import static com.edusphere.enums.RolesEnum.TEACHER;

@Component
public class TestUtils {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuthController authController;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public UserEntity saveUser(String username, String password, OrganizationEntity organizationEntity,
                               RoleEntity roleEntity) {

        UserEntity admin = new UserEntity();
        admin.setUsername(username);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = passwordEncoder.encode(password);

        admin.setPassword(hashedPassword);
        admin.setOrganization(organizationEntity);
        admin.addRole(roleEntity);
        userRepository.save(admin);

        return admin;
    }

    public RoleEntity saveRole(String role, OrganizationEntity organizationEntity) {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setName(role);
        roleEntity.setOrganization(organizationEntity);
        return roleRepository.save(roleEntity);
    }

    public IncidentEntity saveIncident(OrganizationEntity organizationEntity){
        IncidentEntity incidentEntity = new IncidentEntity();
        ChildEntity childEntity = saveAChildInOrganization(organizationEntity);
        incidentEntity.setChild(childEntity);
        incidentEntity.setSummary(generateRandomString());
        incidentEntity.setAcknowledged(false);

        return incidentRepository.save(incidentEntity);
    }

    public OrganizationEntity saveOrganization() {
        return saveOrganization(generateRandomString(), generateRandomString());
    }

    private OrganizationEntity saveOrganization(String name, String description) {
        OrganizationEntity organizationEntity = new OrganizationEntity();
        organizationEntity.setName(name);
        organizationEntity.setDescription(description);
        organizationRepository.save(organizationEntity);
        return organizationEntity;
    }

    public UserEntity saveAParentInOrganization(OrganizationEntity organizationEntity) {
        RoleEntity parentRole = saveRole(PARENT.getName(), organizationEntity);
        return saveUser(generateRandomString(), "123456", organizationEntity, parentRole);
    }

    public UserEntity saveATeacherInOrganization(OrganizationEntity organizationEntity) {
        RoleEntity teacherRole = saveRole(TEACHER.getName(), organizationEntity);
        return saveUser(generateRandomString(), "123456", organizationEntity, teacherRole);
    }

    public UserEntity saveAParentInAnotherOrganization() {
        OrganizationEntity aRandomOrganization = saveOrganization();
        RoleEntity parentRole = saveRole(PARENT.getName(), aRandomOrganization);
        return  saveUser(generateRandomString(), "asddas", aRandomOrganization, parentRole);
    }

    public ClassEntity saveAClassInOrganization(OrganizationEntity organizationEntity){
        ClassEntity classEntity = new ClassEntity();
        classEntity.setName(generateRandomString());
        classEntity.setOrganization(organizationEntity);

        return classRepository.save(classEntity);
    }

    public ClassEntity saveAClassInAnotherOrganization(){
        OrganizationEntity organizationEntity = saveOrganization();
        ClassEntity classEntity = new ClassEntity();
        classEntity.setName(generateRandomString());
        classEntity.setOrganization(organizationEntity);

        return classRepository.save(classEntity);
    }

    public ChildEntity saveAChildInOrganization(OrganizationEntity organizationEntity){
        UserEntity aParent = saveAParentInOrganization(organizationEntity);
        return saveAChildWithParentInOrganization(organizationEntity, aParent);
    }

    public ChildEntity saveAChildWithParentInOrganization(OrganizationEntity organizationEntity, UserEntity parentEntity){
        ClassEntity classEntity = saveAClassInOrganization(organizationEntity);

        ChildEntity childEntity = new ChildEntity();
        childEntity.setName(generateRandomString());
        childEntity.setSurname(generateRandomString());
        childEntity.setClassEntity(classEntity);
        childEntity.setParent(parentEntity);
        childEntity.setBaseTax(1000);

        return childRepository.save(childEntity);
    }

    public InvoiceEntity saveInvoice(OrganizationEntity organizationEntity){
        InvoiceEntity invoiceEntity = new InvoiceEntity();
        ChildEntity childEntity = saveAChildInOrganization(organizationEntity);
        invoiceEntity.setChild(childEntity);
        invoiceEntity.setAmount(1000);
        invoiceEntity.setDueDate(LocalDate.now());
        invoiceEntity.setIssueDate(LocalDate.now());
        invoiceEntity.setPayType(PaymentTypeEnum.TRANSFER);

        return invoiceRepository.save(invoiceEntity);
    }

    public InvoiceEntity saveAPaidInvoice(OrganizationEntity organizationEntity){
        InvoiceEntity invoiceEntity = new InvoiceEntity();
        ChildEntity childEntity = saveAChildInOrganization(organizationEntity);
        invoiceEntity.setChild(childEntity);
        invoiceEntity.setAmount(1000);
        invoiceEntity.setDueDate(LocalDate.now());
        invoiceEntity.setIssueDate(LocalDate.now());
        invoiceEntity.setPayType(PaymentTypeEnum.TRANSFER);
        invoiceEntity.setIsPaid(true);

        return invoiceRepository.save(invoiceEntity);
    }


    public InvoiceEntity saveInvoiceForChildOnMonth(ChildEntity childEntity,
                                                    LocalDate month){
        InvoiceEntity invoiceEntity = new InvoiceEntity();
        invoiceEntity.setChild(childEntity);
        invoiceEntity.setAmount(1000);
        invoiceEntity.setDueDate(month);
        invoiceEntity.setIssueDate(month);
        invoiceEntity.setPayType(PaymentTypeEnum.TRANSFER);

        return invoiceRepository.save(invoiceEntity);
    }

    public ChildEntity saveAChildInAnotherOrganization(){
        OrganizationEntity anotherOrganization = saveOrganization();
        UserEntity aParent = saveAParentInOrganization(anotherOrganization);

        ChildEntity childEntity = new ChildEntity();
        childEntity.setName(generateRandomString());
        childEntity.setSurname(generateRandomString());
        ClassEntity classEntity = saveAClassInOrganization(anotherOrganization);
        childEntity.setClassEntity(classEntity);
        childEntity.setParent(aParent);
        childEntity.setBaseTax(3000);

        return childRepository.save(childEntity);
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getTokenForUser(String userName, String password) {
        LoginRequestVO requestVO = new LoginRequestVO();
        requestVO.setUsername(userName);
        requestVO.setPassword(password);
        return authController.authenticateUser(requestVO).getBody();
    }

    public static String generateRandomString() {
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            int randomIndex = RANDOM.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(randomIndex);
            sb.append(randomChar);
        }
        return sb.toString();
    }
}
