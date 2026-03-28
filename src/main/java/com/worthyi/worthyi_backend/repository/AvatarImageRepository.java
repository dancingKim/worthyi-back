package com.worthyi.worthyi_backend.repository;

import com.worthyi.worthyi_backend.model.entity.AvatarImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AvatarImageRepository extends JpaRepository<AvatarImage, Long> {

    List<AvatarImage> findAllByUser_UserIdOrderByCreatedAtDesc(UUID userId);

    Optional<AvatarImage> findByAvatarImageIdAndUser_UserId(Long avatarImageId, UUID userId);

    void deleteByUser_UserId(UUID userId);
}
