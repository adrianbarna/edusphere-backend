package com.edusphere.repositories;

import com.edusphere.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<UserEntity> findByUsername(@Param("username") String username);

    List<UserEntity> findByOrganizationId(Integer organizationId);

    Optional<UserEntity> findByIdAndOrganizationId(Integer id, Integer organizationId);

    boolean existsByIdAndOrganizationId(Integer id, Integer organizationId);

    void deleteByIdAndOrganizationId(Integer id, Integer organizationId);
}
