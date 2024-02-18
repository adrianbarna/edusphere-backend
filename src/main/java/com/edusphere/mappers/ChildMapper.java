package com.edusphere.mappers;

import com.edusphere.entities.ChildEntity;
import com.edusphere.repositories.ClassRepository;
import com.edusphere.repositories.UserRepository;
import com.edusphere.vos.ChildVO;
import org.springframework.stereotype.Component;

@Component
public class ChildMapper {
    private final UserRepository userRepository;
    private final ClassRepository classRepository;

    public ChildMapper(UserRepository userRepository, ClassRepository classRepository) {
        this.userRepository = userRepository;
        this.classRepository = classRepository;
    }

    public ChildEntity toEntity(ChildVO childVO) {
        if (childVO == null) {
            return null;
        }

        return ChildEntity.builder()
                .id(childVO.getId())
                .name(childVO.getName())
                .surname(childVO.getSurname())
                .parent(userRepository.findById(childVO.getParentId()).orElse((null)))
                .classEntity(classRepository.findById(childVO.getClassId()).orElse(null))
                .build();
    }

    public ChildVO toVO(ChildEntity childEntity) {
        return ChildVO.builder()
                .id(childEntity.getId())
                .name(childEntity.getName())
                .surname(childEntity.getSurname())
                .parentId(childEntity.getParent() != null ? childEntity.getParent().getId() : null)
                .classId(childEntity.getClassEntity() != null ? childEntity.getClassEntity().getId() : null)
                .build();
    }
}
