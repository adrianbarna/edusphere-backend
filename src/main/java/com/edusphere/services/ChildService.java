package com.edusphere.services;

import com.edusphere.entities.ChildEntity;
import com.edusphere.entities.ClassEntity;
import com.edusphere.entities.UserEntity;
import com.edusphere.exceptions.ChildNotFoundException;
import com.edusphere.exceptions.ClassNotFoundException;
import com.edusphere.exceptions.ParentNotFoundException;
import com.edusphere.mappers.ChildMapper;
import com.edusphere.repositories.ChildRepository;
import com.edusphere.repositories.ClassRepository;
import com.edusphere.repositories.UserRepository;
import com.edusphere.vos.ChildVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChildService {

    private final ChildRepository childRepository;
    private final ChildMapper childMapper;
    private final UserRepository userRepository;
    private final ClassRepository classRepository;

    public ChildService(ChildRepository childRepository, ChildMapper childMapper, UserRepository userRepository, ClassRepository classRepository) {
        this.childRepository = childRepository;
        this.childMapper = childMapper;
        this.userRepository = userRepository;
        this.classRepository = classRepository;
    }

    public ChildVO addChild(ChildVO childVO, Integer organizationId) {
        ChildEntity childEntity = childMapper.toEntity(childVO, organizationId);
        if (!organizationId.equals(childEntity.getParent().getOrganization().getId())) {
            throw new ParentNotFoundException(childVO.getParentId());
        }
        ChildEntity savedChild = childRepository.save(childEntity);
        return childMapper.toVO(savedChild);
    }

    public ChildVO updateChild(Integer childId, ChildVO childVO, Integer organizationId) {
        ChildEntity existingChild = childRepository.findByIdAndParentOrganizationId(childId, organizationId)
                .orElseThrow(() -> new ChildNotFoundException(childId));
        UserEntity parentEntity = userRepository.findByIdAndOrganizationId(childVO.getParentId(), organizationId)
                .orElseThrow(() -> new ParentNotFoundException(childVO.getParentId()));
        ClassEntity classEntity = classRepository.findByIdAndOrganizationId(childVO.getClassId(), organizationId)
                .orElseThrow(() -> new ClassNotFoundException(childVO.getId()));

        existingChild.setName(childVO.getName());
        existingChild.setSurname(childVO.getSurname());
        existingChild.setParent(parentEntity);
        existingChild.setClassEntity(classEntity);


        ChildEntity updatedChild = childRepository.save(existingChild);
        return childMapper.toVO(updatedChild);
    }

    //TODO this method is not used
    public boolean deleteChild(Integer childId, Integer organizationId) {
        if (childRepository.existsByIdAndParentOrganizationId(childId, organizationId)) {
            childRepository.deleteByIdAndParentOrganizationId(childId, organizationId);
            return true;
        }
        return false;
    }

    public ChildVO getChildById(Integer childId, Integer organizationId) {
        ChildEntity childEntity = childRepository.findByIdAndParentOrganizationId(childId, organizationId)
                .orElseThrow(() -> new RuntimeException("Copilul cu id-ul " + childId+ " nu a fost gasit"));
        return childMapper.toVO(childEntity);
    }

    public List<ChildVO> getAllChildren(Integer organizationId) {
        List<ChildEntity> childEntities = childRepository.findByParentOrganizationId(organizationId);
        return childEntities.stream()
                .map(childMapper::toVO)
                .collect(Collectors.toList());
    }

    public List<ChildVO> getChildrenByParentId(Integer parentId, Integer organizationId) {
        List<ChildEntity> childEntities = childRepository.findByParentIdAndParentOrganizationId(parentId, organizationId);
        if(childEntities.isEmpty()){
            throw new ChildNotFoundException("Parintele cu id-ul: "+parentId+" nu are niciun copil in organizatie");
        }
        return childEntities.stream()
                .map(childMapper::toVO)
                .collect(Collectors.toList());
    }
}
