package com.worthyi.worthyi_backend.repository;

import com.worthyi.worthyi_backend.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByAuthorityName(String authorityName);
}
