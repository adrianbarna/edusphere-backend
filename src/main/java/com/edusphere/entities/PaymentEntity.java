package com.edusphere.entities;

import com.edusphere.enums.PaymentTypeEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;


@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "payments")
public class PaymentEntity {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "child_id", nullable = false)
    private ChildEntity child;

    @NotNull
    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid= false;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "pay_type", nullable = false, length = Integer.MAX_VALUE)
    private PaymentTypeEnum payType;

    @NotNull
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "due_date")
    private LocalDate dueDate;
}