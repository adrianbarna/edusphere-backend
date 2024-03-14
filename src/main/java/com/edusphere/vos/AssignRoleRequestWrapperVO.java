package com.edusphere.vos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class AssignRoleRequestWrapperVO {
    private Integer userId;
    private List<Integer> roleIds = new ArrayList<>();
}
