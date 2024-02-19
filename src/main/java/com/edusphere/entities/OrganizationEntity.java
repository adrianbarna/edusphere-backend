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
@Table(name = "organizations")
public class OrganizationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "name", nullable = false, columnDefinition = "VARCHAR(255) unique")
    private String name;


    @NotNull
    @Column(name = "description", nullable = false, columnDefinition = "VARCHAR(255)")
    private String description;

    @OneToMany(mappedBy = "organization")
    private Set<ClassEntity> classEntities = new LinkedHashSet<>();

    @OneToMany(mappedBy = "organization")
    private Set<RoleEntity> roles = new LinkedHashSet<>();

    @OneToMany(mappedBy = "organization")
    private Set<UserEntity> users = new LinkedHashSet<>();

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<ClassEntity> getClassEntities() {
        return classEntities;
    }

    public void setClassEntities(Set<ClassEntity> classEntities) {
        this.classEntities = classEntities;
    }

    public Set<RoleEntity> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleEntity> roles) {
        this.roles = roles;
    }

    public Set<UserEntity> getUsers() {
        return users;
    }

    public void setUsers(Set<UserEntity> users) {
        this.users = users;
    }
}