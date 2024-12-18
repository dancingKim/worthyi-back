package com.worthyi.worthyi_backend.model.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "village_stat")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class VillageStat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "village_stat_id")
    private Long villageStatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "village_template_id", nullable = false)
    @ToString.Exclude
    private VillageTemplate villageTemplate;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "level_up_experience", nullable = false)
    private Integer levelUpExperience;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships

    @OneToMany(mappedBy = "villageStat", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AvatarVillageStat> avatarVillageStats;
}
