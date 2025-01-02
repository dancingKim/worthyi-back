package com.worthyi.worthyi_backend.repository;

import com.worthyi.worthyi_backend.model.entity.Avatar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AvatarRepository extends JpaRepository<Avatar, Long> {
    Optional<Avatar> findByUserUserId(Long userId);
}
