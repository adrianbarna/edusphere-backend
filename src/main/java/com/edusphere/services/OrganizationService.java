package com.edusphere.services;

import com.edusphere.entities.OrganizationEntity;
import com.edusphere.mappers.OrganizationMapper;
import com.edusphere.repositories.OrganizationRepository;
import com.edusphere.vos.OrganizationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;

    @Autowired
    public OrganizationService(OrganizationRepository organizationRepository,
                               OrganizationMapper organizationMapper) {
        this.organizationRepository = organizationRepository;
        this.organizationMapper = organizationMapper;
    }

    @Transactional
    public OrganizationVO addOrganization(OrganizationVO organizationVO){
        OrganizationEntity organizationEntity = organizationRepository.save(organizationMapper.toEntity(organizationVO));
        return organizationMapper.toVO(organizationEntity);
    }

    public List<OrganizationVO> getAllOrganizations() {
        return organizationRepository.findAll().stream()
                .map(organizationMapper::toVO)
                .collect(Collectors.toList());
    }

    public OrganizationVO getOrganizationById(Integer id) {
        return organizationMapper.toVO(organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found")));
    }

    @Transactional
    public void updateOrganization(Integer id, OrganizationVO organizationVO) {
        OrganizationEntity organization = organizationMapper.toEntity(organizationVO);
        organization.setId(id); // Ensure the ID is set for updating
        organizationRepository.save(organization);
    }

    @Transactional
    public void deleteOrganization(Integer id) {
        organizationRepository.deleteById(id);
    }
}
