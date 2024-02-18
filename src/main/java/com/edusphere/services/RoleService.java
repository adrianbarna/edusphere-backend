package com.edusphere.services;

import com.edusphere.entities.RoleEntity;
import com.edusphere.entities.UserEntity;
import com.edusphere.exceptions.RoleAlreadyExistsException;
import com.edusphere.exceptions.RoleNotFoundException;
import com.edusphere.mappers.RoleMapper;
import com.edusphere.mappers.UserMapper;
import com.edusphere.repositories.RoleRepository;
import com.edusphere.utils.RoleUtil;
import com.edusphere.utils.UserUtil;
import com.edusphere.vos.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final UserUtil userUtil;
    private final RoleUtil roleUtil;
    private final UserMapper userMapper;


    public RoleService(RoleRepository roleRepository, RoleMapper roleMapper, UserUtil userUtil, RoleUtil roleUtil, UserMapper userMapper) {
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
        this.userUtil = userUtil;
        this.roleUtil = roleUtil;
        this.userMapper = userMapper;
    }

    public List<RoleVO> getAllRoles(Integer organizationId) {
        return roleRepository.findByOrganizationId(organizationId).stream()
                .map(roleMapper::toVO)
                .collect(Collectors.toList());
    }

    public RoleVO getRoleById(Integer roleId, Integer organizationId) {
        Optional<RoleEntity> roleEntityOptional = roleRepository.findByIdAndOrganizationId(roleId, organizationId);
        return roleEntityOptional.map(roleMapper::toVO).orElseThrow(() -> new RoleNotFoundException(roleId));
    }

    @Transactional
    public RoleVO createRole(CreateUpdateRoleVO createUpdateRoleVO, Integer organizationId) {
        createUpdateRoleVO.setId(null);

        RoleEntity roleEntity = roleMapper.toEntity(createUpdateRoleVO, organizationId);
        RoleEntity savedEntity = roleRepository.save(roleEntity);
        return roleMapper.toVO(savedEntity);
    }

    @Transactional
    public RoleVO updateRole(Integer roleId, CreateUpdateRoleVO createUpdateRoleVO, Integer organizationId) {
        Optional<RoleEntity> existingRole = roleRepository.findByIdAndOrganizationId(roleId, organizationId);
        return existingRole.map(roleEntity -> {
                    roleEntity.setName(createUpdateRoleVO.getName());
                    return roleEntity;
                })
                .map(roleMapper::toVO)
                .orElseThrow(() -> new RoleNotFoundException(roleId));
    }

    @Transactional
    public boolean deleteRole(Integer roleId, Integer organizationId) {
        if (roleRepository.existsByIdAndOrganizationId(roleId, organizationId)) {
            roleRepository.deleteByIdAndOrganizationId(roleId, organizationId);
            return true;
        }
        throw new RoleNotFoundException(roleId);
    }

    @Transactional
    public UserResponseVO assignRoleToUser(AssignRoleRequestWrapperVO assignRoleRequestWrapperVO, Integer organizationId) {
        UserEntity userEntity = userUtil.getUserOrThrowException(assignRoleRequestWrapperVO.getUserId(), organizationId);
        List<RoleEntity> roleEntities = assignRoleRequestWrapperVO.getRoleIds().stream()
                .map(roleId -> roleUtil.getRoleOrThrowException(roleId, organizationId))
                .toList();

        userEntity.getRoles().addAll(roleEntities);

        return userMapper.toVOWithoutPassword(userEntity);
    }

    @Transactional
    public UserResponseVO changeRolesToUser(AssignRoleRequestWrapperVO assignRoleRequestWrapperVO, Integer organizationId) {
        UserEntity userEntity = userUtil.getUserOrThrowException(assignRoleRequestWrapperVO.getUserId(), organizationId);
        List<RoleEntity> roleEntities = assignRoleRequestWrapperVO.getRoleIds().stream()
                .map(roleId -> roleUtil.getRoleOrThrowException(roleId, organizationId))
                .toList();

        HashSet<RoleEntity> newRoles = new HashSet<>(roleEntities);
        userEntity.setRoles(newRoles);

        return userMapper.toVOWithoutPassword(userEntity);
    }
}
