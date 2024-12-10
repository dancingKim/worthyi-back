package com.worthyi.worthyi_backend.model.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "place_instance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id")
    private Long placeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "village_id", nullable = false)
    @ToString.Exclude
    private VillageInstance villageInstance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_template_id", nullable = false)
    @ToString.Exclude
    private PlaceTemplate placeTemplate;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_slot_id")
    @ToString.Exclude
    private PositionSlot positionSlot;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships

    @OneToMany(mappedBy = "placeInstance", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ChildActionInstance> childActionInstances;
}
