package com.worthyi.worthyi_backend.repository;

import com.worthyi.worthyi_backend.model.entity.AvatarInVillage;
import com.worthyi.worthyi_backend.model.entity.AvatarInVillageId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvatarInVillageRepository extends JpaRepository<AvatarInVillage, AvatarInVillageId> {
    // 필요한 쿼리 메소드 추가 가능
    void deleteByAvatar_AvatarIdIn(List<Long> avatarIds);
} 