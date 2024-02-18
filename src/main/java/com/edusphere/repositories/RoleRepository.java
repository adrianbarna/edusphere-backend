package com.edusphere.repositories;

import com.edusphere.entities.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Integer> {
    Optional<RoleEntity> findByIdAndOrganizationId(Integer roleId, Integer organizationId);
    List<RoleEntity> findByOrganizationId(Integer organizationId);

    Boolean existsByIdAndOrganizationId(Integer roleId, Integer organizationId);
    void deleteByIdAndOrganizationId(Integer roleId, Integer organizationId);

    Optional<RoleEntity> findByName(String name);
}

