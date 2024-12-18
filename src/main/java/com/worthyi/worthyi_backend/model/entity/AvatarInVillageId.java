package com.worthyi.worthyi_backend.model.entity;

import lombok.*;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvatarInVillageId implements Serializable{

    @Column(name = "avatar_id")
    private Long avatarId;

    @Column(name = "village_id")
    private Long villageId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AvatarInVillageId that = (AvatarInVillageId) o;

        if (!Objects.equals(avatarId, that.avatarId)) return false;
        return Objects.equals(villageId, that.villageId);
    }

    @Override
    public int hashCode() {
        int result = avatarId != null ? avatarId.hashCode() : 0;
        result = 31 * result + (villageId != null ? villageId.hashCode() : 0);
        return result;
    }
}
