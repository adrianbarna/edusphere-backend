package com.edusphere.utils;

import com.edusphere.entities.UserEntity;
import com.edusphere.exceptions.UserNotFoundException;
import com.edusphere.repositories.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserUtil {
    private final UserRepository userRepository;

    public UserUtil(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity getUserOrThrowException(Integer userId, Integer organizationId) {
        Optional<UserEntity> userEntityOptional = userRepository.findByIdAndOrganizationId(userId, organizationId);
        if (userEntityOptional.isEmpty()) {
            throw new UserNotFoundException("Userul cu id-ul " + userId + " este invalid pentru organizatia " + organizationId);
        }
        return userEntityOptional.get();
    }
}
