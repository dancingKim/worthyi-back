package com.worthyi.worthyi_backend.repository;

import com.worthyi.worthyi_backend.model.entity.PlaceInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlaceInstanceRepository extends JpaRepository<PlaceInstance, Long> {
    Optional<PlaceInstance> findByVillageInstance_VillageId(Long villageId);
}
