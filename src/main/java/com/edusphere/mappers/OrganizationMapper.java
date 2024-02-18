package com.edusphere.mappers;

import com.edusphere.entities.OrganizationEntity;
import com.edusphere.vos.OrganizationVO;
import org.springframework.stereotype.Component;

@Component
public class OrganizationMapper {

    public OrganizationEntity toEntity(OrganizationVO organizationVO){
        if (organizationVO == null) {
            return null;
        }
        return OrganizationEntity.builder()
                .id(organizationVO.getId())
                .name(organizationVO.getName())
                .description(organizationVO.getDescription())
                .build();
    }

    public OrganizationVO toVO(OrganizationEntity organizationEntity){
        if (organizationEntity == null) {
            return null;
        }
        return OrganizationVO.builder()
                .id(organizationEntity.getId())
                .name(organizationEntity.getName())
                .description(organizationEntity.getDescription())
                .build();
    }
}
