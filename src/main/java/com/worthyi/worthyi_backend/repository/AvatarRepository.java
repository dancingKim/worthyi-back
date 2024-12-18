package com.worthyi.worthyi_backend.repository;

import com.worthyi.worthyi_backend.model.entity.Avatar;
import com.worthyi.worthyi_backend.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;

public interface AvatarRepository extends JpaRepository<Avatar, Long> {
}
