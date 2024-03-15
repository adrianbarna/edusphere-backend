package com.edusphere.vos;

import com.edusphere.enums.PaymentTypeEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentVO {
    private Integer id;
    private ChildVO childVO;
    private Integer amount;
    private Boolean isPaid;
    private PaymentTypeEnum payType;
    private Date issueDate;
    private Date dueDate;
}