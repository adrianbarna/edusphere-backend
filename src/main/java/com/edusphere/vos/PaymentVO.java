package com.edusphere.vos;

import com.edusphere.enums.PaymentTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class PaymentVO {
    private Integer id;
    private ChildVO childVO;
    private Integer amountWithoutSkipDays;
    private Integer amountWithSkipDays;
    private Boolean isPaid;
    private PaymentTypeEnum payType;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private List<SkippedDaysVO> skippedDaysVOList = new ArrayList<>();

    public void addSkippedDaysVO(SkippedDaysVO skippedDaysVO){
        skippedDaysVOList.add(skippedDaysVO);
    }
}