package com.edusphere.mappers;

import com.edusphere.entities.RoleEntity;
import com.edusphere.entities.UserEntity;
import com.edusphere.exceptions.OrganizationNotFoundException;
import com.edusphere.repositories.OrganizationRepository;
import com.edusphere.vos.CreateUpdateRoleVO;
import com.edusphere.vos.RoleVO;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class RoleMapper {
    private final OrganizationRepository organizationRepository;

    public RoleMapper(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public RoleEntity toEntity(CreateUpdateRoleVO roleVO, Integer organizationId) {
        return RoleEntity.builder()
                .id(roleVO.getId())
                .name(roleVO.getName())
                .organization(organizationRepository.findById(organizationId)
                        .orElseThrow(() -> new OrganizationNotFoundException(organizationId)))
                .build();
    }

    public RoleVO toVO(RoleEntity roleEntity) {
        return RoleVO.builder()
                .id(roleEntity.getId())
                .name(roleEntity.getName())
                .build();
    }
}
