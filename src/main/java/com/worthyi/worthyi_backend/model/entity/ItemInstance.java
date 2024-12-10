package com.worthyi.worthyi_backend.model.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "item_instance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_instance_id")
    private Long itemInstanceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_template_id", nullable = false)
    @ToString.Exclude
    private ItemTemplate itemTemplate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avatar_id", nullable = false)
    @ToString.Exclude
    private Avatar avatar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "village_id", nullable = false)
    @ToString.Exclude
    private VillageInstance villageInstance;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "is_equipped", nullable = false)
    private Boolean isEquipped;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
}
