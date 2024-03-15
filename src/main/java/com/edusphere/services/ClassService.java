package com.edusphere.services;

import com.edusphere.entities.ClassEntity;
import com.edusphere.entities.OrganizationEntity;
import com.edusphere.entities.UserEntity;
import com.edusphere.exceptions.ClassNotFoundException;
import com.edusphere.exceptions.OrganizationNotFoundException;
import com.edusphere.mappers.ClassMapper;
import com.edusphere.repositories.ClassRepository;
import com.edusphere.repositories.OrganizationRepository;
import com.edusphere.utils.TeacherUtil;
import com.edusphere.vos.ClassVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ClassService {

    private final ClassRepository classRepository;
    private final OrganizationRepository organizationRepository;
    private final ClassMapper classMapper;
    private final TeacherUtil teacherUtil;

    public ClassService(ClassRepository classRepository, OrganizationRepository organizationRepository, ClassMapper classMapper, TeacherUtil teacherUtil) {
        this.classRepository = classRepository;
        this.organizationRepository = organizationRepository;
        this.classMapper = classMapper;
        this.teacherUtil = teacherUtil;
    }

    public List<ClassVO> getAllClasses(Integer organizationId) {
        return classRepository.findByOrganizationId(organizationId).stream()
                .map(classMapper::toVO)
                .toList();
    }

    public ClassVO getClassById(Integer classId, Integer organizationId) {

        Optional<ClassEntity> classEntityOptional = classRepository.findByIdAndOrganizationId(classId, organizationId);

        if (classEntityOptional.isEmpty()) {
            throw new ClassNotFoundException(classId);
        }

        if (!organizationId.equals(classEntityOptional.get().getOrganization().getId())) {
            throw new ClassNotFoundException(classId);
        }
        return classMapper.toVO(classEntityOptional.get());
    }

    @Transactional
    public ClassVO createClass(ClassVO classVO, Integer organizationId) {
        ClassEntity classEntity = classMapper.toEntity(classVO, organizationId);
        setAuthenticathedOrganizationToClassEntity(classEntity, organizationId);
        ClassEntity savedClass = classRepository.save(classEntity);

        savedClass.getTeachers().forEach(teacher -> teacher.setClassEntity(savedClass));
        return classMapper.toVO(savedClass);
    }

    @Transactional
    public ClassVO updateClass(Integer id, ClassVO classVO, Integer organizationId) {
        Optional<ClassEntity> classEntityOptional = classRepository.findByIdAndOrganizationId(id, organizationId);

        if (classEntityOptional.isEmpty()) {
            throw new ClassNotFoundException(id);
        }


        ClassEntity classEntity = classEntityOptional.get();
        classEntity.setName(classVO.getName());

        if (classVO.getTeacherIds() != null && !classVO.getTeacherIds().isEmpty()) {
            Set<UserEntity> teachers = classVO.getTeacherIds().stream()
                    .map(teacherId -> teacherUtil.getTeacherOrThrowException(organizationId, teacherId))
                    .collect(Collectors.toSet());
            classEntity.setTeachers(teachers);
        }

        ClassEntity savedClass = classRepository.save(classEntity);

        savedClass.getTeachers().forEach(teacher -> teacher.setClassEntity(savedClass));
        return classMapper.toVO(savedClass);
    }

    @Transactional
    public void deleteClass(Integer classId, Integer organizationId) {
        if (classRepository.existsByIdAndOrganizationId(classId, organizationId)) {
            classRepository.deleteByIdAndOrganizationId(classId, organizationId);
        } else {
            throw new ClassNotFoundException(classId);
        }
    }

    private void setAuthenticathedOrganizationToClassEntity(ClassEntity classEntity, Integer organizationId) {
        Optional<OrganizationEntity> organizationEntity = organizationRepository.findById(organizationId);
        classEntity.setOrganization(organizationEntity.orElseThrow(() -> new OrganizationNotFoundException(organizationId)));
    }
}
