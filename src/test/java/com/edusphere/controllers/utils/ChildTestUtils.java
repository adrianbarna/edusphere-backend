package com.edusphere.controllers.utils;

import com.edusphere.entities.ChildEntity;
import com.edusphere.entities.ClassEntity;
import com.edusphere.entities.OrganizationEntity;
import com.edusphere.entities.UserEntity;
import com.edusphere.repositories.ChildRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.edusphere.controllers.utils.StringTestUtils.generateRandomString;

@Component
public class ChildTestUtils {

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private UserTestUtils userUtils;

    @Autowired
    private ClassTestUtils classUtils;

    @Autowired
    private OrganizationTestUtils organizationUtils;

    public ChildEntity saveAChildInOrganization(OrganizationEntity organizationEntity){
        UserEntity aParent = userUtils.saveAParentInOrganization(organizationEntity);
        return saveAChildWithParentInOrganization(organizationEntity, aParent);
    }

    public ChildEntity saveAChildWithParentInOrganization(OrganizationEntity organizationEntity, UserEntity parentEntity){
        ClassEntity classEntity = classUtils.saveAClassInOrganization(organizationEntity);

        ChildEntity childEntity = new ChildEntity();
        childEntity.setName(generateRandomString());
        childEntity.setSurname(generateRandomString());
        childEntity.setClassEntity(classEntity);
        childEntity.setParent(parentEntity);
        childEntity.setBaseTax(1000);

        return childRepository.save(childEntity);
    }

    public ChildEntity saveAChildInAnotherOrganization(){
        OrganizationEntity anotherOrganization = organizationUtils.saveOrganization();
        UserEntity aParent = userUtils.saveAParentInOrganization(anotherOrganization);

        ChildEntity childEntity = new ChildEntity();
        childEntity.setName(generateRandomString());
        childEntity.setSurname(generateRandomString());
        ClassEntity classEntity = classUtils.saveAClassInOrganization(anotherOrganization);
        childEntity.setClassEntity(classEntity);
        childEntity.setParent(aParent);
        childEntity.setBaseTax(3000);

        return childRepository.save(childEntity);
    }
}
