package com.worthyi.worthyi_backend.model.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "adult_action_template")
public class AdultActionTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adultActionTemplateId;

    private String name;
    private String description;

    @Column(columnDefinition = "jsonb")
    private String dataSchema;

    @OneToMany(mappedBy = "adultActionTemplate", cascade = CascadeType.ALL)
    private List<AdultActionInstance> adultActionInstances = new ArrayList<>();
}
