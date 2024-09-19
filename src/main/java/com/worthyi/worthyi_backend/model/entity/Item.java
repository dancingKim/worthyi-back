package com.worthyi.worthyi_backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "item_img")
    private String itemImg;

    @Column(name = "item_submit_img")
    private String itemSubmitImg;

    @Column(name = "item_name")
    private String itemName;

    @Column(name = "item_detail_content")
    private String itemDetailContent;

    @Column(name = "item_type_code", nullable = false)
    private String itemTypeCode;

    @ManyToOne
    @JoinColumn(name = "village_template_id", nullable = false)
    private VillageTemplate villageTemplate;

    @OneToMany(mappedBy = "item")
    private List<ItemEffect> itemEffects;

    @OneToMany(mappedBy = "item")
    private List<ItemGenerationDetail> itemGenerationDetails;
}
