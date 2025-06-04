package project.hotel_booking_system.controller;

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
import project.hotel_booking_system.service.AuthenticationService;
import project.hotel_booking_system.service.impl.AuthenticationServiceImpl;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {

    AuthenticationService authenticationService;

    @PostMapping("/token")
    ApiResponseDTO<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest authenticationRequest) {
        return ApiResponseDTO.<AuthenticationResponse>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .message("Authentication successful")
                .result(authenticationService.authenticate(authenticationRequest))
                .build();
    }

    @PostMapping("/introspect")
    ApiResponseDTO<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) {
        return ApiResponseDTO.<IntrospectResponse>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .message("Token introspection successful")
                .result(authenticationService.introspect(request))
                .build();
    }

    @PostMapping("/refresh")
    ApiResponseDTO<AuthenticationResponse> refresh(@RequestBody RefreshRequest refreshRequest) {
        return ApiResponseDTO.<AuthenticationResponse>builder()
                .status(HttpStatus.OK.value())
                .time(LocalDateTime.now())
                .message("Token refreshed successfully")
                .result(authenticationService.refreshToken(refreshRequest))
                .build();
    }

    @PostMapping("/logout")
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
