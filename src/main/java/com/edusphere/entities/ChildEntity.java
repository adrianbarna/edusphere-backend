package com.edusphere.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;


@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "children")
public class ChildEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "surname", nullable = false)
    private String surname;

    @NotNull
    @Column(name = "base_tax", nullable = false)
    private Integer baseTax;

    @NotNull
    @Column(name = "meal_price", nullable = false)
    private Integer mealPrice = 0;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassEntity classEntity;

    @OneToMany(mappedBy = "children")
    private Set<GoalEntity> goals = new LinkedHashSet<>();

    @OneToMany(mappedBy = "child")
    private Set<IncidentEntity> incidents = new LinkedHashSet<>();

    @OneToMany(mappedBy = "child")
    private Set<MessageEntity> messages = new LinkedHashSet<>();

    @OneToMany(mappedBy = "child")
    private Set<PaymentEntity> payments = new LinkedHashSet<>();

    @OneToMany(mappedBy = "child")
    private Set<ReportEntity> reports = new LinkedHashSet<>();

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parent_id", nullable = false)
    private UserEntity parent;

    @OneToMany(mappedBy = "child", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SkippedDaysEntity> skippedDays = new LinkedHashSet<>();

}