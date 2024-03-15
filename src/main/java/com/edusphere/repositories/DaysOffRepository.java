package com.edusphere.repositories;

import com.edusphere.entities.DaysNotChargedEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DaysOffRepository extends JpaRepository<DaysNotChargedEntity, Integer> {

   List<DaysNotChargedEntity> findByOrganizationId(Integer organizationId);
}
