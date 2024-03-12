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

    public Set<SkippedDaysEntity> getSkippedDays() {
        return skippedDays;
    }

    public void setSkippedDays(Set<SkippedDaysEntity> skippedDays) {
        this.skippedDays = skippedDays;
    }

    public UserEntity getParent() {
        return parent;
    }

    public void setParent(UserEntity parent) {
        this.parent = parent;
    }

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

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public ClassEntity getClassEntity() {
        return classEntity;
    }

    public void setClassEntity(ClassEntity classEntity) {
        this.classEntity = classEntity;
    }

    public Set<GoalEntity> getGoals() {
        return goals;
    }

    public void setGoals(Set<GoalEntity> goals) {
        this.goals = goals;
    }

    public Set<IncidentEntity> getIncidents() {
        return incidents;
    }

    public void setIncidents(Set<IncidentEntity> incidents) {
        this.incidents = incidents;
    }

    public Set<MessageEntity> getMessages() {
        return messages;
    }

    public void setMessages(Set<MessageEntity> messages) {
        this.messages = messages;
    }

    public Set<PaymentEntity> getPayments() {
        return payments;
    }

    public void setPayments(Set<PaymentEntity> payments) {
        this.payments = payments;
    }

    public Set<ReportEntity> getReports() {
        return reports;
    }

    public void setReports(Set<ReportEntity> reports) {
        this.reports = reports;
    }

    public Integer getBaseTax() {
        return baseTax;
    }

    public void setBaseTax(Integer baseTax) {
        this.baseTax = baseTax;
    }

    public Integer getMealPrice() {
        return mealPrice;
    }

    public void setMealPrice(Integer mealPrice) {
        this.mealPrice = mealPrice;
    }
}