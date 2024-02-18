package com.edusphere.vos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class IncidentVO {
    private Integer id;
    private Integer childId;
    private String summary;
    private Boolean isAcknowledged;
}
