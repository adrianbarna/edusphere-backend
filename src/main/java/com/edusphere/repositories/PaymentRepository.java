package com.edusphere.repositories;

import com.edusphere.entities.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Integer> {

    Optional<PaymentEntity> findByIdAndChildParentOrganizationId(Integer paymentId, Integer organizationId);

    @Query(value = "SELECT p.* FROM payments p " +
            "JOIN children c ON p.child_id = c.id " +
            "JOIN users u ON c.parent_id = u.id " +
            "WHERE c.id = ?1 AND u.organization_id = ?2 " +
            "AND p.is_paid = true " +
            "ORDER BY p.issue_date DESC " +
            "LIMIT 1", nativeQuery = true)
    Optional<PaymentEntity> findLastPaymentByChildIdAndOrganizationId(Integer childId, Integer organizationId);


    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM PaymentEntity p " +
            "WHERE EXTRACT(YEAR FROM p.issueDate) = ?1 " +
            "AND EXTRACT(MONTH FROM p.issueDate) = ?2")
    boolean existsPaymentsForCurrentMonth(int year, int month);

}
