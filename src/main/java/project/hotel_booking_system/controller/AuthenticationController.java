package project.hotel_booking_system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.hotel_booking_system.dto.request.authentication_request.AuthenticationRequest;
import project.hotel_booking_system.dto.request.authentication_request.IntrospectRequest;
import project.hotel_booking_system.dto.request.authentication_request.LogoutRequest;
import project.hotel_booking_system.dto.request.authentication_request.RefreshRequest;
import project.hotel_booking_system.dto.response.ApiResponseDTO;
import project.hotel_booking_system.dto.response.AuthenticationResponse;
import project.hotel_booking_system.dto.response.IntrospectResponse;
import project.hotel_booking_system.service.authentication.AuthenticationService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(
        name = "Authentication",
        description = "APIs for user registration, login, and token management"
)

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {

    AuthenticationService authenticationService;

    @PostMapping("/token")
    @Operation(
            summary = "User Login",
            description = "Authenticate user with username and password. Returns JWT token for accessing protected endpoints. " +
                    "This endpoint is public and does not require authentication."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Authentication successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request format or missing required fields",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid username or password",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Account has been disabled or locked",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            )
    })
    ApiResponseDTO<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest authenticationRequest) {
        return ApiResponseDTO.<AuthenticationResponse>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .message("Authentication successful")
                .result(authenticationService.authenticate(authenticationRequest))
                .build();
    }

    @PostMapping("/introspect")
    @Operation(
            summary = "Token Validation",
            description = "Validate a JWT token and check if it's still valid. " +
                    "Returns the validation status without requiring authentication."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token introspection completed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request format or missing token",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            )
    })
    ApiResponseDTO<IntrospectResponse> introspect(@Valid @RequestBody IntrospectRequest request) {
        return ApiResponseDTO.<IntrospectResponse>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .message("Token introspection successful")
                .result(authenticationService.introspect(request))
                .build();
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh Token",
            description = "Generate a new access token using a valid refresh token. " +
                    "The old token will be invalidated and a new one will be issued. " +
                    "This endpoint does not require authentication but needs a valid refresh token."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token refreshed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request format or missing token",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired refresh token",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User account has been disabled",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            )
    })
    ApiResponseDTO<AuthenticationResponse> refresh(@RequestBody RefreshRequest refreshRequest) {
        return ApiResponseDTO.<AuthenticationResponse>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .message("Token refreshed successfully")
                .result(authenticationService.refreshToken(refreshRequest))
                .build();
    }

    @PostMapping("/logout")
    @Operation(
            summary = "User Logout",
            description = "Invalidate the current JWT token and log out the user. " +
                    "The token will be added to a blacklist and cannot be used again. " +
                    "This endpoint does not require authentication but needs a valid token."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Logout successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request format or missing token",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class)
                    )
            )
    })
    ApiResponseDTO<AuthenticationResponse> logout(@RequestBody LogoutRequest logoutRequest) {
        authenticationService.logout(logoutRequest);
        return ApiResponseDTO.<AuthenticationResponse>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .message("Logout successful")
                .result(null)
                .build();
    }

}
