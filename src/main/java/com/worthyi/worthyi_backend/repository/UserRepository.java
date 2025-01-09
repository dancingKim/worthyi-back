package com.worthyi.worthyi_backend.repository;

import com.worthyi.worthyi_backend.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userRoles ur LEFT JOIN FETCH ur.role WHERE u.provider = :provider AND u.sub = :sub")
    Optional<User> findByProviderAndSub(@Param("provider") String provider, @Param("sub") String sub);

    Optional<User> findByUserId(UUID userId);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.avatars WHERE u.userId = :userId")
    Optional<User> findByUserIdWithAvatars(@Param("userId") UUID userId);
}