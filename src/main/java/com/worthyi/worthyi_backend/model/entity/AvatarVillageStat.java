package com.worthyi.worthyi_backend.model.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "avatar_village_stat",
        uniqueConstraints = @UniqueConstraint(columnNames = {"avatar_id", "village_id", "village_stat_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvatarVillageStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "avatar_village_stat_id")
    private Long avatarVillageStatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avatar_id", nullable = false)
    @ToString.Exclude
    private Avatar avatar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "village_id", nullable = false)
    @ToString.Exclude
    private VillageInstance villageInstance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "village_stat_id", nullable = false)
    @ToString.Exclude
    private VillageStat villageStat;

    @Column(name = "level")
    private Integer level;

    @Column(name = "current_experience")
    private Integer currentExperience;

    @Column(name = "total_experience")
    private Integer totalExperience;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
