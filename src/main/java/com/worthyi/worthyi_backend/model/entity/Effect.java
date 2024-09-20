package com.worthyi.worthyi_backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "effect")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Effect {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "effect_id")
    private Long effectId;

    @Column(name = "effect_name", nullable = false)
    private String effectName;

    @Column(name = "effect_figure", nullable = false)
    private Integer effectFigure = 0;

    @ManyToOne
    @JoinColumn(name = "village_stat_id", nullable = false)
    private StatTemplate villageStat;

    @OneToMany(mappedBy = "effect")
    private List<ItemEffect> itemEffects;
}
