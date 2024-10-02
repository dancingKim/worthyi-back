package com.worthyi.worthyi_backend.repository;

import java.util.Optional;
import com.worthyi.worthyi_backend.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEid(String eid);
}
