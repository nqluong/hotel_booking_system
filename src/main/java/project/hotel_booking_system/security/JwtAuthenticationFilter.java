package project.hotel_booking_system.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import project.hotel_booking_system.dto.request.authentication_request.IntrospectRequest;
import project.hotel_booking_system.service.AuthenticationService;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationService authenticationService;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            // Validate token using introspect
            IntrospectRequest introspectRequest = new IntrospectRequest();
            introspectRequest.setToken(token);
            var introspectResponse = authenticationService.introspect(introspectRequest);

            if (introspectResponse != null && introspectResponse.isValid()) {
                // If token is valid, set the authentication in SecurityContext
                var authentication = new UsernamePasswordAuthenticationToken(
                    token, // principal
                    null,  // credentials
                    Collections.singleton(new SimpleGrantedAuthority("USER")) // authorities
                );
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // Log the error but don't throw it - just continue the filter chain
            logger.error("Could not authenticate user with token", e);
        }

        filterChain.doFilter(request, response);
    }
} 