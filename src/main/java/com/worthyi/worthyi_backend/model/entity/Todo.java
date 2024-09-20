package com.worthyi.worthyi_backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "todo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todo_id")
    private Long todoId;

    @Column(name = "create_day")
    private LocalDateTime createDay;

    @Column(name = "title")
    private String title;

    @Column(name = "detail_content")
    private String detailContent;

    @Column(name = "todo_status")
    private String todoStatus;

    @ManyToOne
    @JoinColumn(name = "village_id", nullable = false)
    private Village village;

    @OneToMany(mappedBy = "todo")
    private List<ItemGeneration> itemGenerations;
}
