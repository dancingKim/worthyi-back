package com.worthyi.worthyi_backend.model.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "place_template")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_template_id")
    private Long placeTemplateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    @ToString.Exclude
    private VillageTemplate villageTemplate;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "external_background_image")
    private String externalBackgroundImage;

    @Column(name = "internal_background_image")
    private String internalBackgroundImage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_slot_id")
    @ToString.Exclude
    private PositionSlot positionSlot;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships

    @OneToMany(mappedBy = "placeTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ChildActionTemplate> childActionTemplates;

    @OneToMany(mappedBy = "placeTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PlaceInstance> placeInstances;
}
