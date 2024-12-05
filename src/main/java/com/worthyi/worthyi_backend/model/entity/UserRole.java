package com.worthyi.worthyi_backend.model.entity;

import lombok.*;
import jakarta.persistence.*;

@Entity
@Table(name = "user_role")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_role_id")
    private Long userRoleId;

    @JoinColumn(name = "role_id", nullable = false)
    @ToString.Exclude
    @ManyToOne
    private Role role;

    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @ManyToOne
    private User user;
}
