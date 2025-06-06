package project.hotel_booking_system.service.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import project.hotel_booking_system.dto.request.user_request.UserCreateRequest;
import project.hotel_booking_system.dto.request.user_request.UserUpdateRequest;
import project.hotel_booking_system.dto.response.UserResponse;
import project.hotel_booking_system.enums.Role;
import project.hotel_booking_system.model.User;

public interface UserService {

    UserResponse createUser(UserCreateRequest request);

    Page<UserResponse> getAllUsers(Pageable pageable);

    UserResponse getUserById(Long id);

    UserResponse getUserByUsername(String username);

    UserResponse updateUser(Long id, UserUpdateRequest request);

    void deleteUser(Long id);

    UserResponse updateUserRole(Long id, Role role);

    User findUserEntityById(Long id);
} 