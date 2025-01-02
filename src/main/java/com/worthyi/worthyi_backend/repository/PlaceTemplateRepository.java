package com.worthyi.worthyi_backend.repository;

import com.worthyi.worthyi_backend.model.entity.PlaceTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceTemplateRepository extends JpaRepository<PlaceTemplate, Long> {
}