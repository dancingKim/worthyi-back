package com.worthyi.worthyi_backend.repository;

import java.util.Optional;
import com.worthyi.worthyi_backend.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEid(String eid);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userRoles ur LEFT JOIN FETCH ur.role WHERE u.eid = :eid")
    Optional<User> findByEidWithRoles(@Param("eid") String eid);
}
