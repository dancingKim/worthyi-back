package com.worthyi.worthyi_backend.repository;

import com.worthyi.worthyi_backend.model.entity.VillageInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VillageInstanceRepository extends JpaRepository<VillageInstance, Long> {
    Optional<VillageInstance> findByVillageId(Long villageId);
    
    Optional<VillageInstance> findByUserUserId(Long userId);
}