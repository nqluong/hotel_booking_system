package project.hotel_booking_system.configuration;

import java.io.IOException;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import project.hotel_booking_system.dto.response.ApiResponseDTO;
import project.hotel_booking_system.exception.ErrorCode;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) 
            throws IOException, ServletException {
        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;
        String message = getAuthenticationErrorMessage(authException);

        response.setStatus(errorCode.getHttpStatusCode().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponseDTO<?> apiResponse = ApiResponseDTO.builder()
                .status(errorCode.getHttpStatusCode().value())
                .time(LocalDateTime.now())
                .success(false)
                .message(message)
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        response.flushBuffer();
    }

    private String getAuthenticationErrorMessage(AuthenticationException authException) {
        if (authException.getCause() instanceof JwtException) {
            return "Invalid or expired JWT token";
        } else if (authException instanceof BadCredentialsException) {
            return "Invalid credentials provided";
        } else if (authException instanceof InsufficientAuthenticationException) {
            return "Authentication token is missing";
        } else {
            return "Authentication failed: " + authException.getMessage();
        }
    }
}
