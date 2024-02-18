package com.edusphere.vos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseVO {
    private Integer id;
    private String username;
    private String surname;
    private String name;
    private Boolean isActivated;
    private List<RoleVO> roleVOList;

}

