package com.edusphere.repositories;

import com.edusphere.entities.IncidentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentRepository extends JpaRepository<IncidentEntity, Integer> {
    List<IncidentEntity> findAllByChildParentOrganizationId(Integer organizationId);
    Optional<IncidentEntity> findByIdAndChildParentOrganizationId(Integer incidentId, Integer organizationId);
    boolean existsByIdAndChildParentOrganizationId(Integer incidentId,Integer organizationId);
    void deleteByIdAndChildParentOrganizationId(Integer incidentId,Integer organizationId);
}
