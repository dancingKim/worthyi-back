package com.worthyi.worthyi_backend.model.entity;

import lombok.*;
import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "position_slot")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PositionSlot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "position_slot_id")
    private Long positionSlotId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    @ToString.Exclude
    private VillageTemplate villageTemplate;

    @Column(name = "slot_number", nullable = false)
    private Integer slotNumber;

    @Column(name = "position_x", nullable = false)
    private Double positionX;

    @Column(name = "position_y", nullable = false)
    private Double positionY;

    // Relationships

    @OneToMany(mappedBy = "positionSlot", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PlaceTemplate> placeTemplates;
}
