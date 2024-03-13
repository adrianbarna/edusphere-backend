package com.edusphere.controllers.utils;

import com.edusphere.entities.ClassEntity;
import com.edusphere.entities.OrganizationEntity;
import com.edusphere.repositories.ClassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.edusphere.controllers.utils.StringTestUtils.generateRandomString;

@Component
public class ClassTestUtils {


        @Autowired
        private ClassRepository classRepository;

        @Autowired
        private OrganizationTestUtils organizationUtils;


        public ClassEntity saveAClassInOrganization(OrganizationEntity organizationEntity){
            ClassEntity classEntity = new ClassEntity();
            classEntity.setName(generateRandomString());
            classEntity.setOrganization(organizationEntity);

            return classRepository.save(classEntity);
        }

        public ClassEntity saveAClassInAnotherOrganization(){
            OrganizationEntity organizationEntity = organizationUtils.saveOrganization();
            ClassEntity classEntity = new ClassEntity();
            classEntity.setName(generateRandomString());
            classEntity.setOrganization(organizationEntity);

            return classRepository.save(classEntity);
        }



}
