package project.hotel_booking_system.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import project.hotel_booking_system.dto.request.authentication_request.IntrospectRequest;
import project.hotel_booking_system.service.AuthenticationService;

import javax.crypto.spec.SecretKeySpec;

@Component
public class CustomerJwtDecoder implements JwtDecoder {
    @Value( "${jwt.signer-key}" )
    private String signerKey;

    @Autowired
    private AuthenticationService authenticationService;

    private NimbusJwtDecoder nimbusJwtDecoder = null;


    @Override
    public Jwt decode(String token) throws JwtException {
        try{
            var response = authenticationService.introspect(
                    IntrospectRequest.builder()
                            .token(token)
                            .build());

            if (!response.isValid()) {
                throw new JwtException("Token invalid");
            }
        } catch (Exception e) {
            throw new JwtException(e.getMessage());
        }

        if(nimbusJwtDecoder == null) {
            SecretKeySpec secetKeySpec = new SecretKeySpec(signerKey.getBytes(), "HS512");
            nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secetKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();
        }

        return nimbusJwtDecoder.decode(token);
    }
}
