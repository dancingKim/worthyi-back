package com.worthyi.worthyi_backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "oauth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Oauth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "oauth_id")
    private Long oauthId;

    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "provider_nm")
    private String providerNm;

    @Column(name = "social_id")
    private String socialId;

    @ManyToOne
    @JoinColumn(name = "user_uuid", nullable = false)
    private User user;
}
