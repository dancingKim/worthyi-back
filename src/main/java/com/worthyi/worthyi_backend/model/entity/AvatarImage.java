package com.worthyi.worthyi_backend.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "avatar_image")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AvatarImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "avatar_image_id")
    private Long avatarImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "image_key", nullable = false, length = 255, unique = true)
    private String imageKey;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "prompt", columnDefinition = "text")
    private String prompt;

    @Column(name = "generation_model", nullable = false, length = 100)
    private String generationModel;

    @Column(name = "source_type", nullable = false, length = 30)
    private String sourceType;

    @Column(name = "reference_type", nullable = false, length = 30)
    private String referenceType;

    @Column(name = "reference_avatar_image_id")
    private Long referenceAvatarImageId;
}
