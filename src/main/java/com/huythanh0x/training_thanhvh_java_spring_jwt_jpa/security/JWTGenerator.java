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

/**
 * Class to generate, validate, and manipulate JWT tokens.
 */
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

    /**
     * Retrieves the exception message associated with this object.
     *
     * @return The exception message as a String.
     */
    public String getExceptionMessage() {
        return exceptionMessage;
    }

    /**
     * Generates a token based on the authentication details provided.
     *
     * @param authentication The authentication details used to generate the token.
     * @return A string token generated for the provided authentication details.
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(authentication.getName());
        return generateToken(userDetails);
    }

    /**
     * Retrieves the username from a JSON Web Token (JWT).
     *
     * @param token the JWT token from which to extract the username
     * @return the username extracted from the JWT token
     */
    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /**
     * Validates a JWT token by parsing its claims and verifying its signature.
     *
     * @param token The JWT token to be validated
     * @return true if the token is valid, false otherwise
     */
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

    /**
     * Checks if a JWT token is expired or valid.
     *
     * @param token the JWT token to be checked
     * @return true if the token is expired or valid, false otherwise
     * @throws BadRequestException if any other exception occurs during token parsing
     */
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

    /**
     * Generates a JSON Web Token (JWT) for the given user details.
     *
     * @param userDetails the user details for which the token is generated
     * @return the generated JWT as a string
     */
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
