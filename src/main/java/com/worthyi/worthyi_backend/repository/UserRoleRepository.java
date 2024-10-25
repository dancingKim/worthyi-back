package com.worthyi.worthyi_backend.repository;

import com.worthyi.worthyi_backend.model.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    // 사용자의 UUID를 기반으로 역할 목록 조회
    List<UserRole> findByUser_UserUuid(UUID userUuid);
}
