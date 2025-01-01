package com.worthyi.worthyi_backend.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Setter
@Getter
@Table(name = "child_action_instance")
public class ChildActionInstance extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long childActionInstanceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_action_template_id", nullable = false)
    private ChildActionTemplate childActionTemplate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false) // place_id 라는 FK 컬럼 필요
    private PlaceInstance placeInstance; // 이 필드를 추가

    private Long avatarId;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String data; // JSON 형태의 아이 행동 데이터

    @Builder.Default
    @OneToMany(mappedBy = "childActionInstance", cascade = CascadeType.ALL)
    private List<AdultActionInstance> adultActionInstances = new ArrayList<>();

    @Column(name = "created_at", nullable = true, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
