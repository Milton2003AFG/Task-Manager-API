package io.github.milton2003afg.taskmanagerapi;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class JwtValidationIntegrationTest extends AbstractIntegrationTest {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.issuer}")
    private String issuer;

    private String buildToken(String tokenIssuer, String signingSecret, Instant expiresAt)
            throws JOSEException {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(tokenIssuer)
                .subject("1")
                .issueTime(Date.from(Instant.now()))
                .expirationTime(Date.from(expiresAt))
                .build();

        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS512), claims);
        jwt.sign(new MACSigner(signingSecret.getBytes(StandardCharsets.UTF_8)));
        return jwt.serialize();
    }

    @Test
    void controlToken_builtByTest_isAccepted() throws Exception {
        String token = buildToken(issuer, secret, Instant.now().plusSeconds(3600));

        mockMvc.perform(get("/projects")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void tamperedToken_returns401() throws Exception {
        String token = registerAndLogin(uniqueEmail(), PASSWORD);

        String[] parts = token.split("\\.");
        char first = parts[2].charAt(0);
        char replacement = first == 'A' ? 'B' : 'A';
        String tampered = parts[0] + "." + parts[1] + "." + replacement + parts[2].substring(1);

        mockMvc.perform(get("/projects")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tampered))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenWithWrongIssuer_returns401() throws Exception {
        String token = buildToken("another-issuer", secret, Instant.now().plusSeconds(3600));

        mockMvc.perform(get("/projects")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void expiredToken_returns401() throws Exception {
        String token = buildToken(issuer, secret, Instant.now().minusSeconds(300));

        mockMvc.perform(get("/projects")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenSignedWithAnotherSecret_returns401() throws Exception {
        String otherSecret = "x".repeat(64);
        String token = buildToken(issuer, otherSecret, Instant.now().plusSeconds(3600));

        mockMvc.perform(get("/projects")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}
