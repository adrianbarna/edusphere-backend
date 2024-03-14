package com.edusphere.services;

import com.edusphere.entities.IncidentEntity;
import com.edusphere.exceptions.ChildNotFoundException;
import com.edusphere.exceptions.IncidentNotFoundException;
import com.edusphere.mappers.IncidentMapper;
import com.edusphere.repositories.ChildRepository;
import com.edusphere.repositories.IncidentRepository;
import com.edusphere.vos.IncidentVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentMapper incidentMapper;
    private final ChildRepository childRepository;

    public IncidentService(IncidentRepository incidentRepository, IncidentMapper incidentMapper, ChildRepository childRepository) {
        this.incidentRepository = incidentRepository;
        this.incidentMapper = incidentMapper;
        this.childRepository = childRepository;
    }

    @Transactional(readOnly = true)
    public List<IncidentVO> getAllIncidents(Integer organizationId) {
        List<IncidentEntity> incidents = incidentRepository.findAllByChildParentOrganizationId(organizationId);
        return incidents.stream()
                .map(incidentMapper::toVO)
                .toList();
    }

    @Transactional(readOnly = true)
    public IncidentVO getIncidentById(Integer id, Integer organizationId) {
        return incidentRepository.findByIdAndChildParentOrganizationId(id, organizationId)
                .map(incidentMapper::toVO)
                .orElseThrow(() -> new IncidentNotFoundException(id));
    }

    @Transactional
    public IncidentVO createIncident(IncidentVO incidentVO, Integer organizationId) {
        IncidentEntity incidentEntity = incidentMapper.toEntity(incidentVO, organizationId);
        return incidentMapper.toVO(incidentRepository.save(incidentEntity));
    }

    @Transactional
    public IncidentVO updateIncident(Integer id, IncidentVO incidentVO, Integer organizationId) {
        incidentVO.setId(id);
        IncidentEntity updatedEntity = incidentRepository.findByIdAndChildParentOrganizationId(id, organizationId)
                .map(incidentEntity -> {
                    incidentEntity.setAcknowledged(incidentVO.getIsAcknowledged());
                    incidentEntity.setSummary(incidentVO.getSummary());
                    incidentEntity.setChild(childRepository.findByIdAndParentOrganizationId(incidentVO.getChildId(), organizationId)
                            .orElseThrow(() -> new ChildNotFoundException(incidentVO.getChildId())));
                    return incidentEntity;
                })
                .orElseThrow(() -> new IncidentNotFoundException(id));
        incidentRepository.save(updatedEntity);
        return incidentMapper.toVO(updatedEntity);
    }

    @Transactional
    public void deleteIncident(Integer id, Integer organizationId) {

        if (incidentRepository.existsByIdAndChildParentOrganizationId(id, organizationId)) {

            incidentRepository.deleteByIdAndChildParentOrganizationId(id, organizationId);
        }else{
            throw new IncidentNotFoundException(id);
        }
    }

}
