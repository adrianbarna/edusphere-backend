package com.edusphere.repositories;

import com.edusphere.entities.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceEntity, Integer> {

    @Query(value = "SELECT i.* FROM invoices i " +
            "JOIN children c ON i.child_id = c.id " +
            "JOIN users u ON c.parent_id = u.id " +
            "WHERE c.id = ?1 " +
            "AND EXTRACT(MONTH FROM i.issue_date) = ?2 " +
            "AND EXTRACT(YEAR FROM i.issue_date) = ?3 " +
            "AND u.organization_id = ?4", nativeQuery = true)
    List<InvoiceEntity> findByChildIdAndMonthAndYearAndOrganizationId(Integer childId, Integer month, Integer year, Integer organizationId);

    @Query(value = "SELECT i.* FROM invoices i " +
            "JOIN children c ON i.child_id = c.id " +
            "JOIN users u ON c.parent_id = u.id " +
            "WHERE c.parent_id = ?1 " +
            "AND EXTRACT(MONTH FROM i.issue_date) = ?2 " +
            "AND EXTRACT(YEAR FROM i.issue_date) = ?3 " +
            "AND u.organization_id = ?4", nativeQuery = true)
    List<InvoiceEntity> findByParentIdAndMonthAndYearAndOrganizationId(Integer parentId, Integer month, Integer year, Integer organizationId);
}
