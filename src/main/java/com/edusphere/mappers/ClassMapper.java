package com.edusphere.mappers;

import com.edusphere.entities.ClassEntity;
import com.edusphere.entities.UserEntity;
import com.edusphere.exceptions.OrganizationNotFoundException;
import com.edusphere.repositories.OrganizationRepository;
import com.edusphere.utils.TeacherUtil;
import com.edusphere.vos.ClassVO;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ClassMapper {
    private final TeacherUtil teacherUtil;
    private final OrganizationRepository organizationRepository;


    public ClassMapper(TeacherUtil teacherUtil, OrganizationRepository organizationRepository) {
        this.teacherUtil = teacherUtil;
        this.organizationRepository = organizationRepository;
    }

    public ClassEntity toEntity(ClassVO classVO) {
        if (classVO == null) {
            return null;
        }

        Set<UserEntity> teachers = new HashSet<>();
        if(classVO.getTeacherIds() != null) {
            teachers = classVO.getTeacherIds().stream()
                    .map(teacherId -> teacherUtil.getTeacherOrThrowException(classVO, teacherId))
                    .collect(Collectors.toSet());
        }


        return ClassEntity.builder()
                .id(classVO.getId())
                .name(classVO.getName())
                .organization(organizationRepository.findById(classVO.getOrganizationId())
                        .orElseThrow(() -> new OrganizationNotFoundException(classVO.getOrganizationId() )))
                .teachers(teachers)
                .build();
    }

    public ClassVO toVO(ClassEntity classEntity) {
        if (classEntity == null) {
            return null;
        }
        return ClassVO.builder()
                .id(classEntity.getId())
                .name(classEntity.getName())
                .organizationId(classEntity.getOrganization().getId())
                .teacherIds(classEntity.getTeachers().stream()
                        .map(UserEntity::getId)
                        .collect(Collectors.toList()))
                .build();
    }


}
