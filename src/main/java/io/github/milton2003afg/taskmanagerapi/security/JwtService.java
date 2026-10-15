package io.github.milton2003afg.taskmanagerapi.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.github.milton2003afg.taskmanagerapi.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final JWSSigner signer;

    public JwtService(@Value("${app.jwt.secret}") String secret) {
        try {
            this.signer = new MACSigner(secret.getBytes(StandardCharsets.UTF_8));
        } catch (JOSEException ex) {
            throw new IllegalStateException("app.jwt.secret is invalid", ex);
        }
    }

    public String generateToken(User user){
        try {
            JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                    .issuer("http://localhost:8080")
                    .subject(user.getId().toString())
                    .claim("email", user.getEmail())
                    .issueTime(new Date())
                    .expirationTime(Date.from(Instant.now().plusSeconds(1800)))
                    .build();

            JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);
            SignedJWT signedJWT = new SignedJWT(jwsHeader, jwtClaimsSet);
            signedJWT.sign(this.signer);

            return signedJWT.serialize();
        }catch (JOSEException ex){
            throw new IllegalStateException("Could not sign the JWT", ex);
        }
    }
}
