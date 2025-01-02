package com.worthyi.worthyi_backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvatarTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long avatarTemplateId;
    
    private String name;
    private String description;
    private String appearance;
} 