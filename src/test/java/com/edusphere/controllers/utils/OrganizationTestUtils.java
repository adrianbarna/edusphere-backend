package com.edusphere.controllers.utils;

import com.edusphere.entities.OrganizationEntity;
import com.edusphere.repositories.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrganizationTestUtils {

    @Autowired
    private OrganizationRepository organizationRepository;


    public OrganizationEntity saveOrganization() {
        return saveOrganization(StringTestUtils.generateRandomString(), StringTestUtils.generateRandomString());
    }

    private OrganizationEntity saveOrganization(String name, String description) {
        OrganizationEntity organizationEntity = new OrganizationEntity();
        organizationEntity.setName(name);
        organizationEntity.setDescription(description);
        organizationRepository.save(organizationEntity);
        return organizationEntity;
    }
}
