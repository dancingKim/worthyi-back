package com.worthyi.worthyi_backend.repository;

import com.worthyi.worthyi_backend.model.entity.ItemTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemTemplateRepository extends JpaRepository<ItemTemplate, Long> {
    List<ItemTemplate> findByVillageTemplate_TemplateId(Long templateId);
    List<ItemTemplate> findByAvailableInVillage_TemplateId(Long templateId);
} 