package com.worthyi.worthyi_backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "adult_action_template")
public class AdultActionTemplate extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adultActionTemplateId;

    private String name;
    private String description;

    @Column(columnDefinition = "jsonb")
    private String dataSchema;

    @OneToMany(mappedBy = "adultActionTemplate", cascade = CascadeType.ALL)
    @Builder.Default
    private List<AdultActionInstance> adultActionInstances = new ArrayList<>();
}
