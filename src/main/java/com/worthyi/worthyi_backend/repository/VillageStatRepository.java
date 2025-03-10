package com.worthyi.worthyi_backend.repository;

import com.worthyi.worthyi_backend.model.entity.VillageStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VillageStatRepository extends JpaRepository<VillageStat, Long> {
    List<VillageStat> findByVillageTemplate_TemplateId(Long templateId);
} 