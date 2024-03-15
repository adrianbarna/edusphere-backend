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
import java.util.stream.Stream;

@Component
public class ClassMapper {
    private final TeacherUtil teacherUtil;
    private final OrganizationRepository organizationRepository;


    public ClassMapper(TeacherUtil teacherUtil, OrganizationRepository organizationRepository) {
        this.teacherUtil = teacherUtil;
        this.organizationRepository = organizationRepository;
    }

    public ClassEntity toEntity(ClassVO classVO, Integer organizationId) {
        if (classVO == null) {
            return null;
        }

        Set<UserEntity> teachers = new HashSet<>();
        if(classVO.getTeacherIds() != null) {
            teachers = classVO.getTeacherIds().stream()
                    .map(teacherId -> teacherUtil.getTeacherOrThrowException(organizationId, teacherId))
                    .collect(Collectors.toSet());
        }


        return ClassEntity.builder()
                .id(classVO.getId())
                .name(classVO.getName())
                .organization(organizationRepository.findById(organizationId)
                        .orElseThrow(() -> new OrganizationNotFoundException(organizationId)))
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
                .teacherIds(classEntity.getTeachers().stream()
                        .map(UserEntity::getId)
                        .toList())
                .build();
    }


}
