package com.worthyi.worthyi_backend.repository;

import com.worthyi.worthyi_backend.model.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    // 사용자의 ID를 기반으로 역할 목록 조회
    List<UserRole> findByUser_UserId(UUID userId);

    // 사용자 ID로 UserRole 삭제
    void deleteByUser_UserId(UUID userId);
}
