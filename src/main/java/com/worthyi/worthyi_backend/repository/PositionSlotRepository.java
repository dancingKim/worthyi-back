package com.worthyi.worthyi_backend.repository;

import com.worthyi.worthyi_backend.model.entity.PositionSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PositionSlotRepository extends JpaRepository<PositionSlot, Long> {
    List<PositionSlot> findByVillageTemplate_TemplateId(Long templateId);
} 