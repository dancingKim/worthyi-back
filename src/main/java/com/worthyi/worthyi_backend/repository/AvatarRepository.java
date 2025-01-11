package com.worthyi.worthyi_backend.repository;

import com.worthyi.worthyi_backend.model.entity.Avatar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.UUID;

import java.util.Optional;
import java.util.List;

@Repository
public interface AvatarRepository extends JpaRepository<Avatar, Long> {
    @Query("SELECT a FROM Avatar a JOIN FETCH a.user u WHERE u.userId = :userId")
    Optional<Avatar> findByUserUserId(@Param("userId") UUID userId);

    // 사용자 ID로 아바타 삭제
    void deleteByUser_UserId(UUID userId);

    @Query("SELECT a.avatarId FROM Avatar a WHERE a.user.userId = :userId")
    List<Long> findAvatarIdsByUserId(@Param("userId") UUID userId);
}
