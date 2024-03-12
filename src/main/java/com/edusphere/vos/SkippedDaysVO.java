package com.edusphere.vos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
@Builder
public class SkippedDaysVO {
    private Integer id;
    private Date startDate;
    private Date endDate;
    private Integer childId;
    private Boolean proccessed;
    private Integer amount;
}
