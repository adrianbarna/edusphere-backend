package com.edusphere.mappers;

import com.edusphere.entities.IncidentEntity;
import com.edusphere.exceptions.ChildNotFoundException;
import com.edusphere.repositories.ChildRepository;
import com.edusphere.vos.IncidentVO;
import org.springframework.stereotype.Component;

@Component
public class IncidentMapper {
    private final ChildRepository childRepository;

    public IncidentMapper(ChildRepository childRepository) {
        this.childRepository = childRepository;
    }

    public IncidentVO toVO(IncidentEntity incidentEntity) {
        return IncidentVO.builder()
                .id(incidentEntity.getId())
                .childId(incidentEntity.getChild().getId())
                .isAcknowledged(incidentEntity.getAcknowledged())
                .summary(incidentEntity.getSummary())
                .build();
    }

    public IncidentEntity toEntity(IncidentVO incidentVO, Integer organizationId) {
        return IncidentEntity.builder()
                .id(incidentVO.getId())
                .child(childRepository.findByIdAndParentOrganizationId(incidentVO.getId(),
                        organizationId).orElseThrow(() -> new ChildNotFoundException(incidentVO.getId())))
                .isAcknowledged(incidentVO.getIsAcknowledged())
                .summary(incidentVO.getSummary())
                .build();
    }
}
