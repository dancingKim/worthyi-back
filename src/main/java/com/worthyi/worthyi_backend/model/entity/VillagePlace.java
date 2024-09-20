package com.worthyi.worthyi_backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "village_place")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VillagePlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "village_place_id")
    private Long villagePlaceId;

    @Column(name = "village_place_name")
    private String villagePlaceName;

    @Column(name = "village_place_role_code")
    private String villagePlaceRoleCode;

    @Column(name = "village_place_img")
    private String villagePlaceImg;

    @ManyToOne
    @JoinColumn(name = "village_template_id", nullable = false)
    private VillageTemplate villageTemplate;
}
