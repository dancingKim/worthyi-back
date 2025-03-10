package com.worthyi.worthyi_backend.repository;

import com.worthyi.worthyi_backend.model.entity.AvatarVillageStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AvatarVillageStatRepository extends JpaRepository<AvatarVillageStat, Long> {
    List<AvatarVillageStat> findByAvatar_AvatarId(Long avatarId);
    List<AvatarVillageStat> findByVillageInstance_VillageId(Long villageId);
    void deleteByAvatar_User_UserId(UUID userId);
} 