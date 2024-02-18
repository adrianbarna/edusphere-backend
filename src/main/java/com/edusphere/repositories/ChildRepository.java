package com.edusphere.repositories;


import com.edusphere.entities.ChildEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChildRepository extends JpaRepository<ChildEntity, Integer> {
    // Custom queries can be added here if needed

    List<ChildEntity> findByParentIdAndParentOrganizationId(Integer parentId, Integer organizationId);

    List<ChildEntity> findByParentOrganizationId(Integer organizationId);

    Optional<ChildEntity> findByIdAndParentOrganizationId(Integer childId, Integer organizationId);

    boolean existsByIdAndParentOrganizationId(Integer childId, Integer organizationId);

    void deleteByIdAndParentOrganizationId(Integer childId, Integer organizationId);

}
