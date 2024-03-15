package com.edusphere.vos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChildVO {
    private Integer id;
    private String name;
    private String surname;
    private Integer parentId;
    private Integer classId;
    private Integer baseTax;
    private Integer mealPrice;
}
