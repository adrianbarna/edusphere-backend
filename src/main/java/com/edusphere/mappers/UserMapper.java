package com.edusphere.mappers;

import com.edusphere.entities.UserEntity;
import com.edusphere.repositories.OrganizationRepository;
import com.edusphere.vos.RoleVO;
import com.edusphere.vos.UserRequestVO;
import com.edusphere.vos.UserResponseVO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Component
public class UserMapper {
    private final OrganizationRepository organizationRepository;
    private final RoleMapper roleMapper;

    public UserMapper(OrganizationRepository organizationRepository, RoleMapper roleMapper) {
        this.organizationRepository = organizationRepository;
        this.roleMapper = roleMapper;
    }

    public UserEntity toEntity(UserRequestVO userRequestVO) {
        if (userRequestVO == null) {
            return null;
        }
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = passwordEncoder.encode(userRequestVO.getPassword());
        userRequestVO.setPassword(hashedPassword);

        return UserEntity.builder()
                .id(userRequestVO.getId())
                .username(userRequestVO.getUsername())
                .name(userRequestVO.getName())
                .surname(userRequestVO.getSurname())
                .organization(userRequestVO.getOrganizationId() != null ? organizationRepository.getReferenceById(userRequestVO.getOrganizationId()) : null)
                .password(hashedPassword)
                .isActivated(userRequestVO.getIsActivated())
                .build();
    }

    public UserResponseVO toVOWithoutPassword(UserEntity userEntity) {
        if (userEntity == null) {
            return null;
        }
        List<RoleVO> roles = Optional.ofNullable(userEntity.getRoles())
                .orElse(new HashSet<>()).stream()
                .map(roleMapper::toVO)
                .toList();

        return UserResponseVO.builder()
                .id(userEntity.getId())
                .username(userEntity.getUsername())
                .name(userEntity.getName())
                .surname(userEntity.getSurname())
                .isActivated(userEntity.getIsActivated())
                .roleVOList(roles)
                .build();
    }
}
