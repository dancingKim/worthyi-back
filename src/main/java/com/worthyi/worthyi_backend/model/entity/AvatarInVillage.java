package com.worthyi.worthyi_backend.model.entity;

import lombok.*;
import jakarta.persistence.*;

@Entity
@Table(name = "avatar_in_village")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AvatarInVillage extends BaseEntity {

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
}
