package com.edusphere.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "classes")
public class ClassEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", length = Integer.MAX_VALUE)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private OrganizationEntity organization;

    @OneToMany(mappedBy = "classEntity")
    private Set<ChildEntity> children = new LinkedHashSet<>();

    @OneToMany(mappedBy = "classEntity")
    private Set<NewsletterEntity> newsletters = new LinkedHashSet<>();

    @OneToMany(mappedBy = "classEntity")
    private Set<UserEntity> teachers = new LinkedHashSet<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Set<ChildEntity> getChildren() {
        return children;
    }

    public void setChildren(Set<ChildEntity> children) {
        this.children = children;
    }

    public Set<NewsletterEntity> getNewsletters() {
        return newsletters;
    }

    public void setNewsletters(Set<NewsletterEntity> newsletters) {
        this.newsletters = newsletters;
    }

    public Set<UserEntity> getTeachers() {
        return teachers;
    }

    public void setTeachers(Set<UserEntity> teachers) {
        this.teachers = teachers;
    }
}