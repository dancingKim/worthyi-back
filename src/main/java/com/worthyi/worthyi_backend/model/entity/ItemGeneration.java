package com.worthyi.worthyi_backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "item_generation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemGeneration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_generation_id")
    private Long itemGenerationId;

    @Column(name = "generation_datetime")
    private LocalDateTime generationDatetime;

    @Column(name = "api_use")
    private Boolean apiUse = false;

    @Column(name = "api_name")
    private String apiName;

    @Column(name = "api_key")
    private String apiKey;

    @ManyToOne
    @JoinColumn(name = "todo_id", nullable = false)
    private Todo todo;

    @OneToMany(mappedBy = "itemGeneration")
    private List<ItemGenerationDetail> itemGenerationDetails;
}
