package project.hotel_booking_system.controller;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.hotel_booking_system.dto.request.UserCreateRequest;
import project.hotel_booking_system.dto.request.UserUpdateRequest;
import project.hotel_booking_system.dto.response.ApiResponseDTO;
import project.hotel_booking_system.dto.response.PaginationResponse;
import project.hotel_booking_system.dto.response.UserResponse;
import project.hotel_booking_system.enums.Role;
import project.hotel_booking_system.service.UserService;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseDTO<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        log.info("Creating new user");
        UserResponse userResponse = userService.createUser(request);
        return ApiResponseDTO.<UserResponse>builder()
                .status(HttpStatus.CREATED.value())
                .time(LocalDateTime.now())
                .success(true)
                .message("User created successfully")
                .result(userResponse)
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponseDTO<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("Updating user with ID: {}", id);
        UserResponse userResponse = userService.updateUser(id, request);
        return ApiResponseDTO.<UserResponse>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .success(true)
                .message("User updated successfully")
                .result(userResponse)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponseDTO<UserResponse> getUserById(@PathVariable Long id) {
        log.info("Getting user with ID: {}", id);
        UserResponse userResponse = userService.getUserById(id);
        return ApiResponseDTO.<UserResponse>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .success(true)
                .message("User retrieved successfully")
                .result(userResponse)
                .build();
    }

    @GetMapping
    public ApiResponseDTO<PaginationResponse<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        log.info("Getting all users with page: {}, size: {}", page, size);
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;
        
        Page<UserResponse> userPage = userService.getAllUsers(
                PageRequest.of(page, size, Sort.by(sortDirection, sortBy)));
        
        PaginationResponse<UserResponse> paginationResponse = PaginationResponse.<UserResponse>builder()
                .content(userPage.getContent())
                .currentPage(userPage.getNumber())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .build();
        
        return ApiResponseDTO.<PaginationResponse<UserResponse>>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .success(true)
                .message("Users retrieved successfully")
                .result(paginationResponse)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponseDTO<Void> deleteUser(@PathVariable Long id) {
        log.info("Soft deleting user with ID: {}", id);
        userService.deleteUser(id);
        return ApiResponseDTO.<Void>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .success(true)
                .message("User deleted successfully")
                .result(null)
                .build();
    }

    @PatchMapping("/{id}/role")
    public ApiResponseDTO<UserResponse> updateUserRole(
            @PathVariable Long id,
            @RequestParam Role role) {
        log.info("Updating role to {} for user with ID: {}", role, id);
        UserResponse userResponse = userService.updateUserRole(id, role);
        return ApiResponseDTO.<UserResponse>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .success(true)
                .message("User role updated successfully")
                .result(userResponse)
                .build();
    }
}
