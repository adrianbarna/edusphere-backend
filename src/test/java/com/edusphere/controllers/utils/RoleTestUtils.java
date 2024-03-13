package com.edusphere.controllers.utils;

import com.edusphere.entities.OrganizationEntity;
import com.edusphere.entities.RoleEntity;
import com.edusphere.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RoleTestUtils {

    @Autowired
    private RoleRepository roleRepository;

    public RoleEntity saveRole(String role, OrganizationEntity organizationEntity) {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setName(role);
        roleEntity.setOrganization(organizationEntity);
        return roleRepository.save(roleEntity);
    }
}
