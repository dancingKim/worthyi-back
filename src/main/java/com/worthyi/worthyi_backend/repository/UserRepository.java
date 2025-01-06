package com.worthyi.worthyi_backend.repository;

import java.util.Optional;

import com.worthyi.worthyi_backend.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderUserId(String providerUserId);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userRoles ur LEFT JOIN FETCH ur.role WHERE u.providerUserId = :providerUserId")
    Optional<User> findByProviderUserIdWithRoles(@Param("providerUserId") String providerUserId);

    boolean existsByPoviderUserId(String ProviderUserId);

    Optional<User> findByUserId(Long userId);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.avatars WHERE u.userId = :userId")
    Optional<User> findByUserIdWithAvatars(@Param("userId") Long userId);
}