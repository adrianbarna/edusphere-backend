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
    private Set<ChildEntity> children = new LinkedHashSet<>();

    @OneToMany(mappedBy = "owner")
    private Set<EventEntity> events = new LinkedHashSet<>();

    @OneToMany(mappedBy = "parent")
    private Set<FeedbackEntity> feedbacks = new LinkedHashSet<>();

    @OneToMany(mappedBy = "teacher")
    private Set<GoalEntity> goals = new LinkedHashSet<>();

    @OneToMany(mappedBy = "parent")
    private Set<MessageEntity> messages = new LinkedHashSet<>();

    @OneToMany(mappedBy = "author")
    private Set<NewsletterEntity> newsletters = new LinkedHashSet<>();

    @OneToMany(mappedBy = "teacher")
    private Set<ReportEntity> reports = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<RoleEntity> roles = new LinkedHashSet<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OrganizationEntity getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationEntity organization) {
        this.organization = organization;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getActivated() {
        return isActivated;
    }

    public void setActivated(Boolean activated) {
        isActivated = activated;
    }

    public ClassEntity getClassEntity() {
        return classEntity;
    }

    public void setClassEntity(ClassEntity classEntity) {
        this.classEntity = classEntity;
    }

    public Set<ChildEntity> getChildren() {
        return children;
    }

    public void setChildren(Set<ChildEntity> children) {
        this.children = children;
    }

    public Set<EventEntity> getEvents() {
        return events;
    }

    public void setEvents(Set<EventEntity> events) {
        this.events = events;
    }

    public Set<FeedbackEntity> getFeedbacks() {
        return feedbacks;
    }

    public void setFeedbacks(Set<FeedbackEntity> feedbacks) {
        this.feedbacks = feedbacks;
    }

    public Set<GoalEntity> getGoals() {
        return goals;
    }

    public void setGoals(Set<GoalEntity> goals) {
        this.goals = goals;
    }

    public Set<MessageEntity> getMessages() {
        return messages;
    }

    public void setMessages(Set<MessageEntity> messages) {
        this.messages = messages;
    }

    public Set<NewsletterEntity> getNewsletters() {
        return newsletters;
    }

    public void setNewsletters(Set<NewsletterEntity> newsletters) {
        this.newsletters = newsletters;
    }

    public Set<ReportEntity> getReports() {
        return reports;
    }

    public void setReports(Set<ReportEntity> reports) {
        this.reports = reports;
    }

    public Set<RoleEntity> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleEntity> roles) {
        this.roles = roles;
    }

    public void addRole(RoleEntity roleEntity) {
        if (roleEntity != null) {
            roles.add(roleEntity);
        }
    }

}