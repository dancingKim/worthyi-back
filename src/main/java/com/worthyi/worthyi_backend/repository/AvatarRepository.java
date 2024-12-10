package com.worthyi.worthyi_backend.repository;

import com.worthyi.worthyi_backend.model.entity.Avatar;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvatarRepository extends JpaRepository<Avatar, Long> {
}
