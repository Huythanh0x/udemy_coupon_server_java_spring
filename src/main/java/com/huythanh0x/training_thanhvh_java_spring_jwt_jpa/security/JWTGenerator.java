package com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.security;

import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.exception.BadRequestException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JWTGenerator {

    //private static final KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);
    String exceptionMessage;
    @Value("${custom.jwt-expiration}")
    Long JWT_EXPIRATION;
    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    private final CustomUserDetailsService userDetailsService;

    public JWTGenerator(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(authentication.getName());
        return generateToken(userDetails);
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SignatureException | MalformedJwtException | ExpiredJwtException | UnsupportedJwtException |
                 IllegalArgumentException | BadCredentialsException ex) {
            exceptionMessage = ex.getMessage();
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            exceptionMessage = "UNEXPECTED EXCEPTION " + ex.getMessage();
            System.out.println("UNEXPECTED EXCEPTION " + ex.getMessage());
        }
        return false;
    }

    public boolean isTokenExpiredOrValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException ex) {
            return true;
        } catch (Exception ex) {
            throw new BadRequestException(ex.getMessage());
        }
    }

    public String generateToken(UserDetails userDetails) {
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + JWT_EXPIRATION);
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
    }
}
