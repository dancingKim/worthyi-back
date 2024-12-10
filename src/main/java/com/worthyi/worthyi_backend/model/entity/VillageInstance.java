package com.worthyi.worthyi_backend.model.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "village_instance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VillageInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "village_id")
    private Long villageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    @ToString.Exclude
    private VillageTemplate villageTemplate;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships

    @OneToMany(mappedBy = "villageInstance", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PlaceInstance> placeInstances;

    @OneToMany(mappedBy = "villageInstance", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AvatarVillageStat> avatarVillageStats;

    @OneToMany(mappedBy = "villageInstance", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AvatarInVillage> avatarInVillages;

}
