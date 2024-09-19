package com.worthyi.worthyi_backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "avatar")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Avatar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "avatar_id")
    private Long avatarId;

    @Column(name = "avatar_nm")
    private String avatarNm;

    @Column(name = "avatar_img")
    private String avatarImg;

    @ManyToOne
    @JoinColumn(name = "user_uuid", nullable = false)
    private User user;

    @OneToMany(mappedBy = "avatar")
    private List<Village> villages;

    @OneToMany(mappedBy = "avatar")
    private List<VillageAvatarStat> villageAvatarStats;
}
