package com.worthyi.worthyi_backend.repository;

import com.worthyi.worthyi_backend.model.entity.Avatar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AvatarRepository extends JpaRepository<Avatar, Long> {
    @Query("SELECT a FROM Avatar a JOIN FETCH a.user u WHERE u.userId = :userId")
    Optional<Avatar> findByUserUserId(@Param("userId") Long userId);
}
