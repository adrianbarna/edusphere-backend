package com.edusphere.utils;

import com.edusphere.entities.UserEntity;
import com.edusphere.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUserUtil {
    private final UserRepository userRepository;

    @Autowired
    public AuthenticatedUserUtil(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Integer getCurrentUserOrganizationId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            UserEntity user = userRepository.findByUsername((String) authentication.getPrincipal())
                    .orElseThrow(() -> new IllegalStateException("Userul nu este autenticat"));
            return user.getOrganization() != null ? user.getOrganization().getId() : null;
        }
        throw new IllegalStateException("User-ul logat nu este asignat acestei organizatii!");
    }
}
