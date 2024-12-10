package com.worthyi.worthyi_backend.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.ArrayList;
import java.util.List;

@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Setter
@Getter
@Table(name = "child_action_instance")
public class ChildActionInstance {
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

    @Column(columnDefinition = "text")
    private String data; // JSON 형태의 아이 행동 데이터

    @OneToMany(mappedBy = "childActionInstance", cascade = CascadeType.ALL)
    private List<AdultActionInstance> adultActionInstances = new ArrayList<>();
}
