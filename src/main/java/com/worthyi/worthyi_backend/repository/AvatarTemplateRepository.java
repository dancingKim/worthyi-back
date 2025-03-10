package com.worthyi.worthyi_backend.repository;

import com.worthyi.worthyi_backend.model.entity.AvatarTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AvatarTemplateRepository extends JpaRepository<AvatarTemplate, Long> {
    // 필요한 쿼리 메소드 추가 가능
} 