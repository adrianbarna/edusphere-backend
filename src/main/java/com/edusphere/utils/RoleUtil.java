package com.edusphere.utils;

import com.edusphere.entities.RoleEntity;
import com.edusphere.exceptions.RoleNotFoundException;
import com.edusphere.repositories.RoleRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RoleUtil {
    private final RoleRepository roleRepository;

    public RoleUtil(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public RoleEntity getRoleOrThrowException(Integer roleId, Integer organizationId) {
        Optional<RoleEntity> roleEntityOptional = roleRepository.findByIdAndOrganizationId(roleId, organizationId);
        if (roleEntityOptional.isEmpty()) {
            throw new RoleNotFoundException(roleId);
        }
        return roleEntityOptional.get();
    }
}
