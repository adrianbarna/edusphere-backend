package com.edusphere.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "incidents")
//TODO this entity is not tested
public class IncidentEntity {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "child_id", nullable = false)
    private ChildEntity child;

    @NotNull
    @Column(name = "summary", nullable = false, length = Integer.MAX_VALUE)
    private String summary;

    @NotNull
    @Column(name = "is_acknowledged", nullable = false)
    private Boolean isAcknowledged = false;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ChildEntity getChild() {
        return child;
    }

    public void setChild(ChildEntity child) {
        this.child = child;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Boolean getAcknowledged() {
        return isAcknowledged;
    }

    public void setAcknowledged(Boolean acknowledged) {
        isAcknowledged = acknowledged;
    }
}