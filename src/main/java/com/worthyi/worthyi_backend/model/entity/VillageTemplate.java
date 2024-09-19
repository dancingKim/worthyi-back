package com.worthyi.worthyi_backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "village_template")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VillageTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "village_template_id")
    private Long villageTemplateId;

    @Column(name = "village_subject")
    private String villageSubject;

    @Column(name = "village_name", nullable = false)
    private String villageName;

    @ManyToOne
    @JoinColumn(name = "template_creator_user_uuid", nullable = false)
    private User templateCreator;

    @OneToMany(mappedBy = "villageTemplate")
    private List<Village> villages;

    @OneToMany(mappedBy = "villageTemplate")
    private List<StatTemplate> statTemplates;

    @OneToMany(mappedBy = "villageTemplate")
    private List<VillagePlace> villagePlaces;

    @OneToMany(mappedBy = "villageTemplate")
    private List<Item> items;
}
