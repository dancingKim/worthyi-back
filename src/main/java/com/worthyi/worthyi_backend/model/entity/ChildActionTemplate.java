package com.worthyi.worthyi_backend.model.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "child_action_template")
public class ChildActionTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long childActionTemplateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_template_id", nullable = false)
    private PlaceTemplate placeTemplate;

    private String name;
    private String description;

    @Column(columnDefinition = "jsonb")
    private String dataSchema;

    @OneToMany(mappedBy = "childActionTemplate", cascade = CascadeType.ALL)
    private List<ChildActionInstance> childActionInstances = new ArrayList<>();
}
