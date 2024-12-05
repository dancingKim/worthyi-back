package com.worthyi.worthyi_backend.model.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "village_template")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VillageTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Long templateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_user_id", nullable = false)
    @ToString.Exclude
    private User creatorUser;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "background_image")
    private String backgroundImage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships

    @OneToMany(mappedBy = "villageTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PositionSlot> positionSlots;

    @OneToMany(mappedBy = "villageTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PlaceTemplate> placeTemplates;

    @OneToMany(mappedBy = "villageTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<VillageStat> villageStats;

    @OneToMany(mappedBy = "villageTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ItemTemplate> itemTemplates;
}
