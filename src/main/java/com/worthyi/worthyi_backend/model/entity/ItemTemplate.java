package com.worthyi.worthyi_backend.model.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "item_template")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_template_id")
    private Long itemTemplateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_user_id", nullable = false)
    @ToString.Exclude
    private User creatorUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "village_template_id", nullable = false)
    @ToString.Exclude
    private VillageTemplate villageTemplate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "available_in_village", nullable = false)
    @ToString.Exclude
    private VillageTemplate availableInVillage;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "item_type", nullable = false, length = 50)
    private String itemType; // 예: 'consumable', 'equipable'

    @Column(name = "effect")
    private String effect; // JSONB 형식의 데이터를 String으로 저장

    @Column(name = "design")
    private String design;

    @Column(name = "equip_requirements")
    private String equipRequirements; // JSONB 형식의 데이터를 String으로 저장

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships

    @OneToMany(mappedBy = "itemTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ItemInstance> itemInstances;
}
