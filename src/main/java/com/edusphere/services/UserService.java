package com.edusphere.services;

import com.edusphere.entities.UserEntity;
import com.edusphere.exceptions.UserNotFoundException;
import com.edusphere.mappers.UserMapper;
import com.edusphere.repositories.UserRepository;
import com.edusphere.utils.ChildUtil;
import com.edusphere.utils.ClassUtil;
import com.edusphere.utils.RoleUtil;
import com.edusphere.utils.UserUtil;
import com.edusphere.vos.UserRequestVO;
import com.edusphere.vos.UserResponseVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserUtil userUtil;
    private final ClassUtil classUtil;
    private final RoleUtil roleUtil;
    private final ChildUtil childUtil;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserMapper userMapper, UserUtil userUtil, ClassUtil classUtil, RoleUtil roleUtil, ChildUtil childUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.userUtil = userUtil;
        this.classUtil = classUtil;
        this.roleUtil = roleUtil;
        this.childUtil = childUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponseVO> getAllUsersByOrganizationWithoutPasswordField(Integer organizationId) {
        return userRepository.findByOrganizationId(organizationId).stream()
                .map(userMapper::toVOWithoutPassword)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponseVO createUser(UserRequestVO userRequestVO, Integer organizationId) {
        userRequestVO.setOrganizationId(organizationId);
        userRequestVO.setIsActivated(false);

        UserEntity userEntity = userMapper.toEntity(userRequestVO);
        userEntity = userRepository.save(userEntity);

        return userMapper.toVOWithoutPassword(userEntity);
    }

    public UserResponseVO getUserById(Integer id, Integer organizationId) {
        Optional<UserEntity> userEntityOptional = userRepository.findByIdAndOrganizationId(id, organizationId);
        return userEntityOptional
                .map(userMapper::toVOWithoutPassword)
                .stream()
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException(id));

    }

    @Transactional
    //TODO should we update also password and activated or not?
    public UserResponseVO updateUser(Integer id, UserRequestVO userRequestVO, Integer organizationId) {
        userRequestVO.setId(id);
        userRequestVO.setOrganizationId(organizationId);

        UserEntity userEntity = userUtil.getUserOrThrowException(id, organizationId);

        userEntity.setUsername(userRequestVO.getUsername());
        userEntity.setSurname(userRequestVO.getSurname());
        userEntity.setName(userRequestVO.getName());
        userEntity.setPassword(passwordEncoder.encode(userRequestVO.getPassword()));
        userEntity.setActivated(userRequestVO.getIsActivated());
        if (userRequestVO.getClassEntityId() != null) {
            userEntity.setClassEntity(classUtil.getClassOrThrowException(userRequestVO.getClassEntityId(),
                    organizationId));
        }
        if (userRequestVO.getChildrenIds() != null) {
            userEntity.setChildren(userRequestVO.getChildrenIds().stream()
                    .map(childId -> childUtil.getChildOrThrowException(childId, organizationId))
                    .collect(Collectors.toSet()));
        }
        if (userRequestVO.getRolesIds() != null) {
            userEntity.setRoles(userRequestVO.getRolesIds().stream()
                    .map(roleId -> roleUtil.getRoleOrThrowException(roleId, organizationId))
                    .collect(Collectors.toSet()));
        }

        //TODO map the fields from below
//        private List<Integer> eventsIds;
//        private List<Integer> feedbacksIds;
//        private List<Integer> goalsIds;
//        private List<Integer> messagesIds;
//        private List<Integer> newslettersIds;
//        private List<Integer> reportsIds;

        userEntity = userRepository.save(userEntity);
        return userMapper.toVOWithoutPassword(userEntity);
    }


    @Transactional
    public boolean deleteUser(Integer id, Integer organizationId) {
        if (userRepository.existsByIdAndOrganizationId(id, organizationId)) {
            userRepository.deleteByIdAndOrganizationId(id, organizationId);
            return true;
        }
        return false;
    }
}
