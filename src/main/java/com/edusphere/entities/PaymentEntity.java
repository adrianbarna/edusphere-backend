package com.edusphere.entities;

import com.edusphere.enums.PaymentTypeEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;


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
    @Builder.Default
    private boolean isPaid= false;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "pay_type")
    //TODO populate it when mark payment as paid
    private PaymentTypeEnum payType;

    @NotNull
    @Column(name = "issue_date", nullable = false)
    private Date issueDate;

    @Column(name = "due_date")
    private Date dueDate;
}