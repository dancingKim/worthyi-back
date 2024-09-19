package com.worthyi.worthyi_backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "village_avatar_stat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VillageAvatarStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "village_avatar_stat_id")
    private Long villageAvatarStatId;

    @Column(name = "avatar_stat", nullable = false)
    private Integer avatarStat = 0;

    @Column(name = "stat_level_figure", nullable = false)
    private Integer statLevelFigure = 0;

    @ManyToOne
    @JoinColumn(name = "avatar_id", nullable = false)
    private Avatar avatar;

    @ManyToOne
    @JoinColumn(name = "village_stat_id", nullable = false)
    private StatTemplate villageStat;
}
