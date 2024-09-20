package com.worthyi.worthyi_backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "stat_template")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "village_stat_id")
    private Long villageStatId;

    @Column(name = "stat_name", nullable = false)
    private String statName;

    @ManyToOne
    @JoinColumn(name = "village_template_id", nullable = false)
    private VillageTemplate villageTemplate;

    @OneToMany(mappedBy = "villageStat")
    private List<VillageAvatarStat> villageAvatarStats;

    @OneToMany(mappedBy = "villageStat")
    private List<Effect> effects;
}
