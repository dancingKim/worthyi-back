package com.worthyi.worthyi_backend.repository;

import com.worthyi.worthyi_backend.model.entity.ItemInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ItemInstanceRepository extends JpaRepository<ItemInstance, Long> {
    List<ItemInstance> findByAvatar_AvatarId(Long avatarId);
    List<ItemInstance> findByVillageInstance_VillageId(Long villageId);
    void deleteByAvatar_User_UserId(UUID userId);
} 