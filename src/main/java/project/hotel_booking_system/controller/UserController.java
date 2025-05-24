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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "User Management", description = "APIs for managing users in the hotel booking system")
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Register a new user",
        description = "Create a new user account. All new users are automatically assigned the CUSTOMER role."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "User registered successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid user data provided",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "409", 
            description = "Username or email already exists",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDTO.class)
            )
        )
    })
    public ApiResponseDTO<UserResponse> createUser(
            @Valid @RequestBody UserCreateRequest request) {
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
    @Operation(
        summary = "Update user information",
        description = "Update an existing user's profile information"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "User information updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid user data provided",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDTO.class)
            )
        )
    })
    public ApiResponseDTO<UserResponse> updateUser(
            @Parameter(description = "User ID", required = true)
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
    @Operation(
        summary = "Get user by ID",
        description = "Retrieve user details by their unique ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "User found successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDTO.class)
            )
        )
    })
    public ApiResponseDTO<UserResponse> getUserById(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long id) {
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
    @Operation(
        summary = "Get all users",
        description = "Retrieve a paginated list of all users"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Users retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDTO.class)
            )
        )
    })
    public ApiResponseDTO<PaginationResponse<UserResponse>> getAllUsers(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            
            @Parameter(description = "Field to sort by", example = "id")
            @RequestParam(defaultValue = "id") String sortBy,
            
            @Parameter(description = "Sort direction (asc or desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String direction) {
        
        log.info("Getting all users with page: {}, size: {}", page, size);
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;
        
        Page<UserResponse> userPage = userService.getAllUsers(
                PageRequest.of(page, size, Sort.by(sortDirection, sortBy)));
        
        PaginationResponse<UserResponse> paginationResponse = PaginationResponse.<UserResponse>builder()
                .content(userPage.getContent())
                .page(userPage.getNumber())
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
    @Operation(
        summary = "Delete user",
        description = "Soft delete a user account (deactivate rather than remove from database)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "User deleted successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDTO.class)
            )
        )
    })
    public ApiResponseDTO<Void> deleteUser(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long id) {
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
    @Operation(
        summary = "Update user role",
        description = "Change a user's role (e.g., from CUSTOMER to ADMIN). This endpoint is restricted to administrators."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "User role updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid role provided",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Unauthorized, insufficient permissions",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDTO.class)
            )
        )
    })
    public ApiResponseDTO<UserResponse> updateUserRole(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long id,
            
            @Parameter(description = "New role to assign", required = true, example = "ADMIN")
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
