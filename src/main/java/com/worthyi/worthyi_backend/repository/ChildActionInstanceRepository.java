package com.worthyi.worthyi_backend.repository;

import com.worthyi.worthyi_backend.model.entity.ChildActionInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ChildActionInstanceRepository extends JpaRepository<ChildActionInstance, Long> {

    @Query(value = "SELECT DISTINCT c FROM ChildActionInstance c " +
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

    @Query("SELECT COUNT(DISTINCT FUNCTION('DATE', c.createdAt)) FROM ChildActionInstance c " +
            "WHERE c.avatarId = :avatarId " +
            "AND c.createdAt >= :startDate " +
            "AND c.createdAt < :endDate")
    int countDistinctDatesByAvatarIdAndDateBetween(
        @Param("avatarId") Long avatarId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    void deleteByAvatarIdIn(List<Long> avatarIds);
}
