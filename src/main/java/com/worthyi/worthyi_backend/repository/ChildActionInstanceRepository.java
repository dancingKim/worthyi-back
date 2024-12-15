package com.worthyi.worthyi_backend.repository;

import com.worthyi.worthyi_backend.model.entity.ChildActionInstance;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ChildActionInstanceRepository extends JpaRepository<ChildActionInstance, Long> {

    @Query(value = "SELECT c FROM ChildActionInstance c " +
            "LEFT JOIN FETCH c.adultActionInstances a " +
            "LEFT JOIN FETCH a.adultActionTemplate " +
            "WHERE c.createdAt >= :startOfDay " +
            "AND c.createdAt < :endOfDay " +
            "AND c.avatarId = :avatarId " +
            "ORDER BY c.createdAt DESC" )
    List<ChildActionInstance> findAllByDateAndAvatarId(@Param("avatarId") Long avatarId,
                                                       @Param("startOfDay") LocalDateTime startOfDay,
                                                       @Param("endOfDay") LocalDateTime endOfDay
                                                       );
}
