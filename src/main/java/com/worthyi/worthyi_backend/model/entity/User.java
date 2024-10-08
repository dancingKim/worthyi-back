package com.worthyi.worthyi_backend.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "\"user\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "user_uuid", updatable = false, nullable = false)
    private UUID userUuid;

    @Column(name = "user_id", unique = true, insertable = false, updatable = false)
    private Long userId;

    @Column(name = "eid", nullable = false)
    private String eid;

    @Column(name = "pwd", nullable = true)
    private String pwd;

    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "provider_nm")
    private String providerNm;

    @Column(name = "authority_nm")
    private String authorityNm;

    // 관계 매핑
    @OneToMany(mappedBy = "user")
    private List<Avatar> avatars;

    @OneToMany(mappedBy = "templateCreator")
    private List<VillageTemplate> villageTemplates;

    @OneToMany(mappedBy = "user")
    private List<UserRole> userRoles;

    @OneToMany(mappedBy = "user")
    private List<Oauth> oauths;
}
