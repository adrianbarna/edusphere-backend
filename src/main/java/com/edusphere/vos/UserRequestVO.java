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
public class UserRequestVO {
    private Integer id;
    private String username;
    private String surname;
    private String name;
    private Integer organizationId;
    private String password;
    private Boolean isActivated;
    private Integer classEntityId;
    private List<Integer> childrenIds;
    private List<Integer> eventsIds;
    private List<Integer> feedbacksIds;
    private List<Integer> goalsIds;
    private List<Integer> messagesIds;
    private List<Integer> newslettersIds;
    private List<Integer> reportsIds;
    private List<Integer> rolesIds;
}

