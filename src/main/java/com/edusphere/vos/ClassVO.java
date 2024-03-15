package com.edusphere.vos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassVO {

    private Integer id;
    private String name;
    private List<Integer> teacherIds;
}
