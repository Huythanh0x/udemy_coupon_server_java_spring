package com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.controller;

import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.dto.AuthResponseDTO;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.dto.LoginDTO;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.dto.RefreshTokenRequestDTO;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.dto.RegisterDTO;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.exception.BadRequestException;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.model.user.RefreshTokenEntity;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.service.RefreshTokenService;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.service.AuthService;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class that handles authentication related API endpoints.
 */
@RestController
@RequestMapping("/api/v1/auth")
@ComponentScan("com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.security")
public class AuthController {
    AuthService authService;
    RefreshTokenService refreshTokenService;

    public AuthController(AuthService authService, RefreshTokenService refreshTokenService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * Handles a POST request to the /login endpoint. Validates user login data, generates a JWT token for the user
     * and saves a refresh token for the user in the database.
     *
     * @param loginDto the login credentials provided by the user
     * @return ResponseEntity containing AuthResponseDTO with JWT token and refresh token
     */
    @PostMapping("login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginDTO loginDto) {
        authService.validateLoginData(loginDto.getUsername(), loginDto.getPassword());
        String tokenForUser = authService.getJwtTokenForCurrentUser(loginDto.getUsername(), loginDto.getPassword());
        RefreshTokenEntity refreshToken = refreshTokenService.saveRefreshToken(loginDto.getUsername());
        return new ResponseEntity<>(new AuthResponseDTO(tokenForUser, refreshToken.getRefreshToken()), HttpStatus.OK);
    }

    /**
     * Handles POST requests to register a new user.
     *
     * @param registerDto the RegisterDTO containing username and password for registration
     * @return ResponseEntity with a message confirming successful registration
     */
    @PostMapping("register")
    public ResponseEntity<String> register(@RequestBody RegisterDTO registerDto) {
        authService.validateRegisterData(registerDto.getUsername(), registerDto.getPassword());
        authService.checkIfUserExist(registerDto.getUsername());
        authService.createUserWith(registerDto.getUsername(), registerDto.getPassword());
        return new ResponseEntity<>("User registered success!", HttpStatus.OK);
    }

    /**
     * Endpoint to refresh an authentication token.
     *
     * @param refreshTokenRequestDTO The request containing the refresh token and access token.
     * @return ResponseEntity containing the new authentication token if successful, otherwise throws BadRequestException.
     */
    @PostMapping("refresh-token")
    public ResponseEntity<AuthResponseDTO> refreshToken(@RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO) {
        if (refreshTokenRequestDTO.getAccessToken() == null || refreshTokenRequestDTO.getAccessToken().isEmpty() || authService.isTokenExpiredOrValid(refreshTokenRequestDTO.getAccessToken())) {
            String newToken = authService.generateNewToken(refreshTokenRequestDTO.getRefreshToken());
            return new ResponseEntity<>(new AuthResponseDTO(newToken, refreshTokenRequestDTO.getRefreshToken()), HttpStatus.OK);
        } else {
            throw new BadRequestException("Token is not valid");
        }
    }
}
