package com.edusphere.controllers.utils;

import com.edusphere.entities.OrganizationEntity;
import com.edusphere.entities.RoleEntity;
import com.edusphere.entities.UserEntity;
import com.edusphere.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import static com.edusphere.enums.RolesEnum.PARENT;
import static com.edusphere.enums.RolesEnum.TEACHER;

@Component
public class UserTestUtils {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleTestUtils roleUtils;

    @Autowired
    private OrganizationTestUtils organizationUtils;


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


    public UserEntity saveAParentInOrganization(OrganizationEntity organizationEntity) {
        RoleEntity parentRole = roleUtils.saveRole(PARENT.getName(), organizationEntity);
        return saveUser(StringTestUtils.generateRandomString(), "123456", organizationEntity, parentRole);
    }

    public UserEntity saveATeacherInOrganization(OrganizationEntity organizationEntity) {
        RoleEntity teacherRole = roleUtils.saveRole(TEACHER.getName(), organizationEntity);
        return saveUser(StringTestUtils.generateRandomString(), "123456", organizationEntity, teacherRole);
    }

    public UserEntity saveAParentInAnotherOrganization() {
        OrganizationEntity aRandomOrganization = organizationUtils.saveOrganization();
        RoleEntity parentRole = roleUtils.saveRole(PARENT.getName(), aRandomOrganization);
        return saveUser(StringTestUtils.generateRandomString(), "asddas", aRandomOrganization, parentRole);
    }
}
