package project.hotel_booking_system.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import project.hotel_booking_system.dto.request.UserCreateRequest;
import project.hotel_booking_system.dto.request.UserUpdateRequest;
import project.hotel_booking_system.dto.response.UserResponse;
import project.hotel_booking_system.enums.Role;
import project.hotel_booking_system.model.User;

public interface UserService {
    UserResponse createUser(UserCreateRequest request);
    UserResponse updateUser(Long id, UserUpdateRequest request);
    UserResponse getUserById(Long id);
    Page<UserResponse> getAllUsers(Pageable pageable);
    void deleteUser(Long id);
    UserResponse updateUserRole(Long id, Role role);
    User findUserEntityById(Long id);
} 