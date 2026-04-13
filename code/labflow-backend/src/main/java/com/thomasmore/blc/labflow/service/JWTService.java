package com.thomasmore.blc.labflow.service;

import com.thomasmore.blc.labflow.entity.auth.User;
import com.thomasmore.blc.labflow.repository.auth.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

@Service
@Transactional("authTransactionManager")
public class JWTService {

    private final UserRepository userRepository;
    private final SecretKey signingKey;

    public JWTService(
            UserRepository userRepository,
            @Value("${labflow.jwt.secret}") String jwtSigningSecret) {
        this.userRepository = userRepository;
        byte[] secretBytes = jwtSigningSecret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException(
                    "labflow.jwt.secret must be at least 32 UTF-8 bytes for HS256 (set JWT_SIGNING_SECRET or labflow.jwt.secret)");
        }
        this.signingKey = Keys.hmacShaKeyFor(secretBytes);
    }

    public String generateToken(User user) {

        Map<String, Object> claims = new HashMap<>();

        String rol = Objects.requireNonNull(userRepository.findByEmail(user.getEmail()).getRol()).getNaam();
        String userId = Objects.requireNonNull(userRepository.findByEmail(user.getEmail()).getId()).toString();
        claims.put("rol", rol);
        claims.put("userId", userId);

        return Jwts.builder()
                .claims(claims)
                .subject(user.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 2 * 60 * 60 * 1000))
                .signWith(signingKey)
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
