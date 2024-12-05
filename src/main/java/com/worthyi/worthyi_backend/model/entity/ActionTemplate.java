package com.worthyi.worthyi_backend.model.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "action_template")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "action_template_id")
    private Long actionTemplateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_template_id", nullable = false)
    @ToString.Exclude
    private PlaceTemplate placeTemplate;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "action_schema")
    private String actionSchema; // JSONB 형식의 데이터를 String으로 저장

    @Column(name = "affects_village_stats")
    private String affectsVillageStats; // JSONB 형식의 데이터를 String으로 저장

    @Column(name = "item_drop_rate")
    private String itemDropRate; // JSONB 형식의 데이터를 String으로 저장

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships

    @OneToMany(mappedBy = "actionTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ActionInstance> actionInstances;
}
