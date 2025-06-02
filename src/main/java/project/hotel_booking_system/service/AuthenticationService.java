package project.hotel_booking_system.service;

import project.hotel_booking_system.dto.request.authentication_request.AuthenticationRequest;
import project.hotel_booking_system.dto.request.authentication_request.IntrospectRequest;
import project.hotel_booking_system.dto.request.authentication_request.LogoutRequest;
import project.hotel_booking_system.dto.response.AuthenticationResponse;
import project.hotel_booking_system.dto.response.IntrospectResponse;

public interface AuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest request);

    IntrospectResponse introspect(IntrospectRequest request);

    AuthenticationResponse refreshToken(String refreshToken);

    void logout(LogoutRequest request);
}
