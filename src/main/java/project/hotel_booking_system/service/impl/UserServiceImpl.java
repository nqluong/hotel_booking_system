package project.hotel_booking_system.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import project.hotel_booking_system.dto.request.user_request.UserCreateRequest;
import project.hotel_booking_system.dto.request.user_request.UserUpdateRequest;
import project.hotel_booking_system.dto.response.UserResponse;
import project.hotel_booking_system.enums.Role;
import project.hotel_booking_system.exception.ResourceAlreadyExistsException;
import project.hotel_booking_system.exception.ResourceNotFoundException;
import project.hotel_booking_system.mapper.UserMapper;
import project.hotel_booking_system.model.User;
import project.hotel_booking_system.repository.UserRepository;
import project.hotel_booking_system.service.UserService;

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
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CUSTOMER);
        User savedUser = userRepository.save(user);
        log.info("User created with ID: {}", savedUser.getId());
        return userMapper.toUserResponse(savedUser);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = findUserEntityById(id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (request.getUsername() != null) {
            if (!isAdmin && !user.getId().equals(getCurrentUserId())) {
                throw new AccessDeniedException("Cannot update username");
            }
            // Check if new username already exists
            if (userRepository.existsByUsernameAndIdNot(request.getUsername(), id)) {
                throw new ResourceAlreadyExistsException("User", "username", request.getUsername());
            }
            user.setUsername(request.getUsername());
        }
        
        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        if (request.getEmail() != null) {
            if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
                throw new ResourceAlreadyExistsException("User", "email", request.getEmail());
            }
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
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public UserResponse getUserById(Long id) {
        User user = findUserEntityById(id);
        return userMapper.toUserResponse(user);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or #username == authentication.principal.username")
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return userMapper.toUserResponse(user);
    }



    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') ")
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(userMapper::toUserResponse);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Long id) {

        Long currentUserId = getCurrentUserId();
        if (id.equals(currentUserId)) {
            throw new IllegalStateException("Cannot delete your own account");
        }

        User user = findUserEntityById(id);
        user.setIsActive(false);
        userRepository.save(user);
        log.info("User soft deleted with ID: {}", id);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUserRole(Long id, Role role) {

        Long currentUserId = getCurrentUserId();
        if (id.equals(currentUserId)) {
            throw new IllegalStateException("Cannot change your own role");
        }

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

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {

            try {
                return Long.parseLong(auth.getName());
            } catch (NumberFormatException e) {
                String username = auth.getName();
                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new IllegalStateException("User not found: " + username));
                return user.getId();
            }
        }
        throw new IllegalStateException("No authenticated user found");
    }
}
