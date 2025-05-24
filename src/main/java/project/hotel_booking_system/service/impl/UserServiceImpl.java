package project.hotel_booking_system.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.hotel_booking_system.dto.request.UserCreateRequest;
import project.hotel_booking_system.dto.request.UserUpdateRequest;
import project.hotel_booking_system.dto.response.UserResponse;
import project.hotel_booking_system.enums.Role;
import project.hotel_booking_system.exception.ResourceAlreadyExistsException;
import project.hotel_booking_system.exception.ResourceNotFoundException;
import project.hotel_booking_system.mapper.UserMapper;
import project.hotel_booking_system.model.User;
import project.hotel_booking_system.repository.UserRepository;
import project.hotel_booking_system.service.UserService;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException("User", "username", request.getUsername());
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("User", "email", request.getEmail());
        }
        
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        User savedUser = userRepository.save(user);
        log.info("User created with ID: {}", savedUser.getId());
        return userMapper.toUserResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = findUserEntityById(id);
        
        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        
        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        
        if (request.getFullname() != null) {
            user.setFullname(request.getFullname());
        }
        
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        
        User updatedUser = userRepository.save(user);
        log.info("User updated with ID: {}", updatedUser.getId());
        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = findUserEntityById(id);
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(userMapper::toUserResponse);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = findUserEntityById(id);
        user.setIsActive(false);
        userRepository.save(user);
        log.info("User soft deleted with ID: {}", id);
    }

    @Override
    @Transactional
    public UserResponse updateUserRole(Long id, Role role) {
        User user = findUserEntityById(id);
        user.setRole(role);
        User updatedUser = userRepository.save(user);
        log.info("User role updated to {} for user with ID: {}", role, id);
        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    public User findUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }
} 