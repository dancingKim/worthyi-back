package com.worthyi.worthyi_backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "item_generation_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemGenerationDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_generation_detail_id")
    private Long itemGenerationDetailId;

    @Column(name = "item_use")
    private Boolean itemUse = false;

    @Column(name = "item_name")
    private String itemName;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne
    @JoinColumn(name = "item_generation_id", nullable = false)
    private ItemGeneration itemGeneration;
}
