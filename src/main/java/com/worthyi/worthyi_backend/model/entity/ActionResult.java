package com.worthyi.worthyi_backend.model.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "action_result")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "action_result_id")
    private Long actionResultId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id", nullable = false)
    @ToString.Exclude
    private ActionInstance actionInstance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avatar_id", nullable = false)
    @ToString.Exclude
    private Avatar avatar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "village_id", nullable = false)
    @ToString.Exclude
    private VillageInstance villageInstance;

    @Column(name = "data")
    private String data; // JSONB 형식의 데이터를 String으로 저장

    @Column(name = "village_stat_changes")
    private String villageStatChanges; // JSONB 형식의 데이터를 String으로 저장

    @Column(name = "item_received")
    private Boolean itemReceived;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_instance_id")
    @ToString.Exclude
    private ItemInstance itemInstance;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
