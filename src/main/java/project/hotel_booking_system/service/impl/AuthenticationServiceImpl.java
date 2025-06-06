package project.hotel_booking_system.service.impl;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import project.hotel_booking_system.dto.request.authentication_request.AuthenticationRequest;
import project.hotel_booking_system.dto.request.authentication_request.IntrospectRequest;
import project.hotel_booking_system.dto.request.authentication_request.LogoutRequest;
import project.hotel_booking_system.dto.request.authentication_request.RefreshRequest;
import project.hotel_booking_system.dto.response.AuthenticationResponse;
import project.hotel_booking_system.dto.response.IntrospectResponse;
import project.hotel_booking_system.exception.AppException;
import project.hotel_booking_system.exception.ErrorCode;
import project.hotel_booking_system.model.InvalidatedToken;
import project.hotel_booking_system.model.User;
import project.hotel_booking_system.repository.InvalidatedTokenRepository;
import project.hotel_booking_system.repository.UserRepository;
import project.hotel_booking_system.service.AuthenticationService;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationServiceImpl implements AuthenticationService {

    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    PasswordEncoder passwordEncoder;

    @NonFinal
    @Value("${jwt.signer-key}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESH_DURATION;

    @Override
    public IntrospectResponse introspect(IntrospectRequest request) {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token, false);
        } catch (AppException | JOSEException | ParseException e) {
            isValid = false;
        }

        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }

    @Override
    public AuthenticationResponse refreshToken(RefreshRequest request) {
        try {
            var signedJWT = verifyToken(request.getToken(), true);

            var jwt = signedJWT.getJWTClaimsSet().getJWTID();
            var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .token(jwt)
                    .invalidatedAt(expiryTime)
                    .build();

            invalidatedTokenRepository.save(invalidatedToken);

            var userName = signedJWT.getJWTClaimsSet().getSubject();
            var user = userRepository.findByUsername(userName)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            if (!user.getIsActive()) {
                throw new AppException(ErrorCode.ACCOUNT_DISABLED);
            }

            var token = generateToken(user);
            return AuthenticationResponse.builder()
                    .token(token)
                    .authenticated(true)
                    .build();
        }catch (ParseException | JOSEException e) {
            log.error("Error while refreshing token: {}", e.getMessage());
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    @Override
    public void logout(LogoutRequest request)  {
        try{
            var signedJWT = verifyToken(request.getToken(), true);
            String jit = signedJWT.getJWTClaimsSet().getJWTID();
            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .token(jit)
                    .invalidatedAt(expiryTime)
                    .build();
            invalidatedTokenRepository.save(invalidatedToken);
        }catch(AppException | ParseException | JOSEException e){
            log.error("Error while logging out: {}", e.getMessage());
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {

        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        var user = userRepository.findByUsername(request.getUsername().trim())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        boolean isPasswordValid = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!isPasswordValid) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
        if(!user.getIsActive()) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }
        var token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    private String generateToken(User user) {
        try {
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

            JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                    .subject(user.getUsername())
                    .issuer("hotel-booking-system")
                    .issueTime(new Date())
                    .expirationTime(new Date(
                            Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()
                    ))
                    .jwtID(UUID.randomUUID().toString())
                    .claim("userId", user.getId())
                    .claim("role", "ROLE_"+user.getRole().name().toUpperCase())
                    .claim("isActive", user.getIsActive())
                    .build();

            Payload payload = new Payload(jwtClaimsSet.toJSONObject());
            JWSObject jwsObject = new JWSObject(header, payload);
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Error generating token", e);
        }
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {

        if (invalidatedTokenRepository.existsByToken(token)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = (isRefresh)
                ? new Date(signedJWT
                .getJWTClaimsSet()
                .getIssueTime()
                .toInstant()
                .plus(REFRESH_DURATION, ChronoUnit.SECONDS)
                .toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean verified = signedJWT.verify(verifier);
        if (!(verified && expiryTime.after(new Date()))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if(invalidatedTokenRepository.existsByToken(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return signedJWT;
    }

    public void invalidateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            String jti = signedJWT.getJWTClaimsSet().getJWTID();
            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .token(jti)
                    .invalidatedAt(expiryTime)
                    .build();
            invalidatedTokenRepository.save(invalidatedToken);
        } catch (ParseException e) {
            log.error("Error parsing token for invalidation: {}", e.getMessage());
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    private StringBuilder buildScope(User user) {
        StringBuilder scope = new StringBuilder();
        if (user.getRole() != null) {
            scope.append("ROLE_");
            scope.append(user.getRole().name().toLowerCase());
        }
        return scope;
    }
}
