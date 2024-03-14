package com.edusphere.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "users")
@Setter
@Getter
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "username", columnDefinition = "VARCHAR(255) unique", unique = true)
    private String username;

    @Column(name = "surname", columnDefinition = "VARCHAR(255)")
    private String surname;

    @Column(name = "name", columnDefinition = "VARCHAR(255)")
    private String name;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private OrganizationEntity organization;

    @NotNull
    @Column(name = "password", nullable = false, columnDefinition = "VARCHAR(255)")
    private String password;

    @Column(name = "is_activated")
    private Boolean isActivated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private ClassEntity classEntity;

    @OneToMany(mappedBy = "parent")
    @Builder.Default
    private Set<ChildEntity> children = new LinkedHashSet<>();

    @OneToMany(mappedBy = "owner")
    @Builder.Default
    private Set<EventEntity> events = new LinkedHashSet<>();

    @OneToMany(mappedBy = "parent")
    @Builder.Default
    private Set<FeedbackEntity> feedbacks = new LinkedHashSet<>();

    @OneToMany(mappedBy = "teacher")
    @Builder.Default
    private Set<GoalEntity> goals = new LinkedHashSet<>();

    @OneToMany(mappedBy = "parent")
    @Builder.Default
    private Set<MessageEntity> messages = new LinkedHashSet<>();

    @OneToMany(mappedBy = "author")
    @Builder.Default
    private Set<NewsletterEntity> newsletters = new LinkedHashSet<>();

    @OneToMany(mappedBy = "teacher")
    @Builder.Default
    private Set<ReportEntity> reports = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<RoleEntity> roles = new LinkedHashSet<>();


    public void addRole(RoleEntity roleEntity) {
        if (roleEntity != null) {
            roles.add(roleEntity);
        }
    }
}