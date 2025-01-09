package com.worthyi.worthyi_backend.repository;

import java.util.Optional;

import com.worthyi.worthyi_backend.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findBySub(String sub);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userRoles ur LEFT JOIN FETCH ur.role WHERE u.sub = :sub")
    Optional<User> findByProviderUserIdWithRoles(@Param("sub") String sub);

    // boolean existsByPoviderUserId(String ProviderUserId);

    Optional<User> findByUserId(UUID userId);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.avatars WHERE u.userId = :userId")
    Optional<User> findByUserIdWithAvatars(@Param("userId") UUID userId);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userRoles ur LEFT JOIN FETCH ur.role WHERE u.provider = :provider AND u.sub = :sub")
    Optional<User> findByProviderAndSub(@Param("provider") String provider, @Param("sub") String sub);
}