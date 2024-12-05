package com.worthyi.worthyi_backend.model.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "action_instance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "action_id")
    private Long actionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    @ToString.Exclude
    private PlaceInstance placeInstance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_template_id")
    @ToString.Exclude
    private ActionTemplate actionTemplate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships

    @OneToMany(mappedBy = "actionInstance", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ActionResult> actionResults;
}
