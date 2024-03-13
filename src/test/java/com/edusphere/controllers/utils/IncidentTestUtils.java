package com.edusphere.controllers.utils;

import com.edusphere.entities.ChildEntity;
import com.edusphere.entities.IncidentEntity;
import com.edusphere.entities.OrganizationEntity;
import com.edusphere.repositories.IncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.edusphere.controllers.utils.StringTestUtils.generateRandomString;

@Component
public class IncidentTestUtils {

        @Autowired
        private IncidentRepository incidentRepository;

        @Autowired
        private ChildTestUtils childUtils;


        public IncidentEntity saveIncident(OrganizationEntity organizationEntity){
            IncidentEntity incidentEntity = new IncidentEntity();
            ChildEntity childEntity = childUtils.saveAChildInOrganization(organizationEntity);
            incidentEntity.setChild(childEntity);
            incidentEntity.setSummary(generateRandomString());
            incidentEntity.setAcknowledged(false);

            return incidentRepository.save(incidentEntity);
        }
}
