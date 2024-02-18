package com.edusphere.repositories;

import com.edusphere.entities.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassRepository extends JpaRepository<ClassEntity, Integer> {
    List<ClassEntity> findByOrganizationId(Integer organizationId);
    Optional<ClassEntity> findByIdAndOrganizationId(Integer classId, Integer organizationId);

    Boolean existsByIdAndOrganizationId(Integer classId, Integer organizationId);
    void deleteByIdAndOrganizationId(Integer classId, Integer organizationId);
}

