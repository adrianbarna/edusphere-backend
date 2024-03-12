package com.edusphere.utils;

import com.edusphere.entities.RoleEntity;
import com.edusphere.entities.UserEntity;
import com.edusphere.exceptions.UserNotFoundException;
import com.edusphere.repositories.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Collectors;

import static com.edusphere.enums.RolesEnum.TEACHER;

@Component
public class TeacherUtil {
    private final UserRepository userRepository;

    public TeacherUtil(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity getTeacherOrThrowException(Integer organizationId, Integer teacherId) {
        Optional<UserEntity> teacherOptional = userRepository.findByIdAndOrganizationId(teacherId, organizationId);
        if(!userHasTeacherRole(teacherOptional)) {
            throw new UserNotFoundException("User-ul asignat nu este un profesor!");
        }
        return teacherOptional.orElseThrow(() -> new UserNotFoundException(teacherId));
    }

    public static boolean userHasTeacherRole(Optional<UserEntity> teacherOptional) {
        return teacherOptional.isPresent() && teacherOptional.get().getRoles().stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toSet()).contains(TEACHER.getName());
    }
}
