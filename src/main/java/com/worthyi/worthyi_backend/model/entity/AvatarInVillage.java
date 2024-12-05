package com.worthyi.worthyi_backend.model.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "avatar_in_village")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvatarInVillage {

    @EmbeddedId
    private AvatarInVillageId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("avatarId")
    @JoinColumn(name = "avatar_id", nullable = false)
    @ToString.Exclude
    private Avatar avatar;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("villageId")
    @JoinColumn(name = "village_id", nullable = false)
    @ToString.Exclude
    private VillageInstance villageInstance;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;
}
