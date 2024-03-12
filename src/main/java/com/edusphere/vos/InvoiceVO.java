package com.edusphere.vos;

import com.edusphere.enums.PaymentTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;


@Data
@AllArgsConstructor
@Builder
public class InvoiceVO {
    private Integer id;
    private ChildVO childVO;
    private Integer amount;
    private Boolean isPaid;
    private PaymentTypeEnum payType;
    private LocalDate issueDate;
    private LocalDate dueDate;
}