package com.thanh0x.coursedeal.security;

import com.thanh0x.coursedeal.model.user.UserEntity;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class TokenProvider {

    @Value("${custom.jwt-secret}")
    private String jwtSecret;

    @Value("${custom.jwt-expiration:3600000}")
    private long jwtExpirationInMs;

    private Key key;

    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.length() < 32) {
            this.key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        } else {
            this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        }
    }

    public String createToken(UserEntity user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }
}
