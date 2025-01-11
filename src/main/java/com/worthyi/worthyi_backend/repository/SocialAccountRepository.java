package com.worthyi.worthyi_backend.repository;

import com.worthyi.worthyi_backend.model.entity.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {
    // 사용자 ID로 소셜 계정 삭제
    void deleteByUser_UserId(UUID userId);
} 