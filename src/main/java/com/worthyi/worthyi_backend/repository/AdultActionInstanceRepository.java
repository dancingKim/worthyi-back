package com.worthyi.worthyi_backend.repository;

import com.worthyi.worthyi_backend.model.entity.AdultActionInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AdultActionInstanceRepository extends JpaRepository<AdultActionInstance, Long> {
    void deleteByUserId(UUID userId);
}
