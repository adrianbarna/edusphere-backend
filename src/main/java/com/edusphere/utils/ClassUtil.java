package com.edusphere.utils;

import com.edusphere.entities.ClassEntity;
import com.edusphere.exceptions.ClassNotFoundException;
import com.edusphere.repositories.ClassRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ClassUtil {
    private final ClassRepository classRepository;

    public ClassUtil(ClassRepository classRepository) {
        this.classRepository = classRepository;
    }

    public ClassEntity getClassOrThrowException(Integer classId, Integer organizationId) {
        Optional<ClassEntity> classEntityOptional = classRepository.findByIdAndOrganizationId(classId, organizationId);
        if (classEntityOptional.isEmpty()) {
            throw new ClassNotFoundException(classId);
        }
        return classEntityOptional.get();
    }
}
