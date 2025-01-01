package com.worthyi.worthyi_backend.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "adult_action_instance")
public class AdultActionInstance extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adultActionInstanceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adult_action_template_id", nullable = false)
    private AdultActionTemplate adultActionTemplate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_action_instance_id", nullable = false)
    private ChildActionInstance childActionInstance;

    private Long userId;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String data; // JSON 형태의 어른 행동 데이터
}
