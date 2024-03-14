package com.edusphere.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "days_not_charged")
@Setter
@Getter
public class DaysNotChargedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private OrganizationEntity organization;

    @NotNull
    @Column(name = "day", nullable = false)
    private Date date;

}