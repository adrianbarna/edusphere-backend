package com.edusphere.repositories;

import com.edusphere.entities.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Integer> {

    @Query(value = "SELECT p.* FROM payments p " +
            "JOIN children c ON p.child_id = c.id " +
            "JOIN users u ON c.parent_id = u.id " +
            "WHERE c.id = ?1 " +
            "AND EXTRACT(MONTH FROM p.issue_date) = ?2 " +
            "AND EXTRACT(YEAR FROM p.issue_date) = ?3 " +
            "AND u.organization_id = ?4", nativeQuery = true)
    List<PaymentEntity> findByChildIdAndMonthAndYearAndOrganizationId(Integer childId, Integer month, Integer year, Integer organizationId);

    @Query(value = "SELECT p.* FROM payments p " +
            "JOIN children c ON p.child_id = c.id " +
            "JOIN users u ON c.parent_id = u.id " +
            "WHERE c.parent_id = ?1 " +
            "AND EXTRACT(MONTH FROM p.issue_date) = ?2 " +
            "AND EXTRACT(YEAR FROM p.issue_date) = ?3 " +
            "AND u.organization_id = ?4", nativeQuery = true)
    List<PaymentEntity> findByParentIdAndMonthAndYearAndOrganizationId(Integer parentId, Integer month, Integer year, Integer organizationId);

    Optional<PaymentEntity> findByIdAndChildParentOrganizationId(Integer paymentId, Integer organizationId);
}
