package com.edusphere.repositories;

import com.edusphere.entities.SkippedDaysEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkippedDaysRepository extends JpaRepository<SkippedDaysEntity, Integer> {

    @Query("SELECT s FROM SkippedDaysEntity s WHERE s.child.id = :childId AND s.proccessed = false")
    List<SkippedDaysEntity> findUnproccessedByChildId(int childId);

}
