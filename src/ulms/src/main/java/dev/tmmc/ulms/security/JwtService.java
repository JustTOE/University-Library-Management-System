package dev.tmmc.ulms.security;

import dev.tmmc.ulms.objects.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private static final int MIN_SECRET_BYTES = 32;

    private final String secret;
    private final long lifetimeMinutes;
    private final Environment environment;
    private SecretKey signingKey;

    public JwtService(
            @Value("${ulms.security.jwt.secret:}") String secret,
            @Value("${ulms.security.jwt.lifetime-minutes:60}") long lifetimeMinutes,
            Environment environment
    ) {
        this.secret = secret;
        this.lifetimeMinutes = lifetimeMinutes;
        this.environment = environment;
    }

    @PostConstruct
    void init() {
        boolean isTestProfile = false;
        for (String profile : environment.getActiveProfiles()) {
            if ("test".equalsIgnoreCase(profile)) {
                isTestProfile = true;
                break;
            }
        }

        if (secret == null || secret.isBlank()) {
            if (isTestProfile) {
                return;
            }
            throw new IllegalStateException(
                    "ulms.security.jwt.secret must be set and >=32 bytes for HS256"
            );
        }

        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "ulms.security.jwt.secret must be set and >=32 bytes for HS256"
            );
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(lifetimeMinutes * 60);
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(signingKey)
                .compact();
    }

    public Optional<Jws<Claims>> parse(String token) {
        if (token == null || token.isBlank() || signingKey == null) {
            return Optional.empty();
        }
        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            return Optional.of(jws);
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public Integer getUserId(Claims c) {
        return Integer.valueOf(c.getSubject());
    }

    public String getRole(Claims c) {
        return c.get("role", String.class);
    }

    public String getEmail(Claims c) {
        return c.get("email", String.class);
    }
}
