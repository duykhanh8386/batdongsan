package com.BTL.Springboot.service.impl;

import com.BTL.Springboot.dto.request.auth.AuthenticationRequest;
import com.BTL.Springboot.dto.request.auth.IntrospectRequest;
import com.BTL.Springboot.dto.request.auth.LogoutRequest;
import com.BTL.Springboot.dto.response.auth.AuthenticationDto;
import com.BTL.Springboot.dto.response.auth.IntrospectDto;
import com.BTL.Springboot.entity.UserAccount;
import com.BTL.Springboot.repository.UserAccountRepository;
import com.BTL.Springboot.service.AuthenticationService;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    @Override
    public AuthenticationDto authenticate(AuthenticationRequest request) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        var userOptional = userAccountRepository.findByUsername(request.getUsername());

        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        UserAccount user = userOptional.get();
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated) {
            throw new RuntimeException("Invalid password");
        }

        var token = generateToken(user);

        return AuthenticationDto.builder().token(token).authenticated(true).build();
    }

    @Override
    public IntrospectDto introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token, false);
        } catch (RuntimeException e) {
            isValid = false;
        }

        return IntrospectDto.builder().valid(isValid).build();
    }

    @Override
    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            verifyToken(request.getToken(), false);
            log.info("Token invalidated successfully for logout");
        } catch (Exception exception) {
            log.info("Token already expired or invalid");
            throw new RuntimeException("Invalid token during logout", exception);
        }
    }

    private String generateToken(UserAccount user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("SpringBoot.BTL.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();

        log.debug("Generated JWT with scope: {}", buildScope(user));

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = isRefresh
                ? new Date(signedJWT
                .getJWTClaimsSet()
                .getIssueTime()
                .toInstant()
                .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
                .toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean verified = signedJWT.verify(verifier);

        if (!verified || !expiryTime.after(new Date())) {
            throw new RuntimeException("Unauthenticated");
        }

        return signedJWT;
    }

    private String buildScope(UserAccount user) {
        String scope = "";
        if (user.getRole() != null) {
            scope = "ROLE_" + user.getRole().getCode().toUpperCase(); // Sử dụng code thay vì roleName
        }
        log.debug("Built scope for user {}: {}", user.getUsername(), scope);
        return scope;
    }
}