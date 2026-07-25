package com.kab.qershi.auth.infrastructure.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct; // Import this
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey; // This pulls from application.properties

    private Key key;
    private final long validityInMilliseconds = 3600000;

    @PostConstruct
    protected void init() {
        // Convert the string secret into a secure Key object
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken(String msisdn, String saccoId, List<String> permissions) {
        return Jwts.builder()
                .setSubject(msisdn)
                .claim("saccoId", saccoId)
                .claim("authorities", permissions)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + validityInMilliseconds))
                .signWith(key) // Uses the injected secret key
                .compact();
    }

    // You will need this key to validate tokens in your filter later
    public Key getKey() {
        return key;
    }
}