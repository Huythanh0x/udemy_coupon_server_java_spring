package com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.service;

import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.exception.BadRequestException;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.model.user.Role;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.model.user.UserEntity;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.repository.RefreshTokenRepository;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.repository.RoleRepository;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.repository.UserRepository;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.security.CustomUserDetailsService;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.security.JWTGenerator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

/**
 * Service class for handling authentication functionalities.
 */
@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTGenerator jwtGenerator;
    private final CustomUserDetailsService userDetailsService;

    private final RefreshTokenRepository refreshTokenRepository;

    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JWTGenerator jwtGenerator, CustomUserDetailsService userDetailsService, RefreshTokenRepository refreshTokenRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtGenerator = jwtGenerator;
        this.userDetailsService = userDetailsService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * Checks if a user with the specified username already exists.
     * Throws a BadRequestException if the username is already taken.
     *
     * @param username The username to check for existence
     * @throws BadRequestException If the username is already taken
     */
    public void checkIfUserExist(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("Username is taken!");
        }
    }

    /**
     * Validates the username and password for registration.
     * Throws a BadRequestException if the username or password is null, or if their length is less than 6 characters.
     *
     * @param username The username to be validated
     * @param password The password to be validated
     * @throws BadRequestException if the username or password is null, or if their length is less than 6 characters
     */
    public void validateRegisterData(String username, String password) {
        if (username == null || password == null) {
            throw new BadRequestException("Invalid username or password, try again !!!");
        }
        if (username.length() < 6 || password.length() < 6) {
            throw new BadRequestException("Username and password can not have length less than 6");
        }
    }

    /**
     * Validates the given username and password.
     *
     * @param username the username to be validated
     * @param password the password to be validated
     * @throws BadRequestException if the username or password is null or empty, or if either has a length less than 6
     */
    public void validateLoginData(String username, String password) {
        if (username == null || password == null) {
            throw new BadRequestException("Invalid username or password, try again !!!");
        }
        if (username.isEmpty() || password.isEmpty()) {
            throw new BadRequestException("Username and password can not have length less than 6");
        }
    }

    /**
     * Generates a JWT token for the current user using the provided username and password.
     *
     * @param username the username of the current user
     * @param password the password of the current user
     * @return the generated JWT token
     * @throws BadCredentialsException if the provided credentials are invalid
     */
    public String getJwtTokenForCurrentUser(String username, String password) throws BadCredentialsException {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtGenerator.generateToken(authentication);
    }

    /**
     * Creates a new user with the given username and password.
     *
     * @param username the username of the new user
     * @param password the password of the new user
     */
    public void createUserWith(String username, String password) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        Optional<Role> roles = roleRepository.findByName("USER");
        roles.ifPresent(role -> user.setRoles(Collections.singletonList(role)));
        userRepository.save(user);
    }

    /**
     * Check if the given access token is expired or still valid.
     * @param accessToken The access token to be checked.
     * @return true if the token is expired or invalid, false if the token is still valid.
     */
    public boolean isTokenExpiredOrValid(String accessToken) {
        return jwtGenerator.isTokenExpiredOrValid(accessToken);
    }

    /**
     * Generates a new JWT token using the provided refresh token.
     *
     * @param refreshToken the refresh token used to retrieve the corresponding user's username
     * @return a new JWT token generated for the user specified by the refresh token
     */
    public String generateNewToken(String refreshToken) {
        String username = refreshTokenRepository.findByRefreshToken(refreshToken).get().getUser().getUsername();
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return jwtGenerator.generateToken(userDetails);
    }

}
