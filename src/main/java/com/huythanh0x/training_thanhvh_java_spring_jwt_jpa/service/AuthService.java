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

    public void checkIfUserExist(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("Username is taken!");
        }
    }

    public void validateRegisterData(String username, String password) {
        if (username == null || password == null) {
            throw new BadRequestException("Invalid username or password, try again !!!");
        }
        if (username.length() < 6 || password.length() < 6) {
            throw new BadRequestException("Username and password can not have length less than 6");
        }
    }

    public void validateLoginData(String username, String password) {
        if (username == null || password == null) {
            throw new BadRequestException("Invalid username or password, try again !!!");
        }
        if (username.isEmpty() || password.isEmpty()) {
            throw new BadRequestException("Username and password can not have length less than 6");
        }
    }

    public String getJwtTokenForCurrentUser(String username, String password) throws BadCredentialsException {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtGenerator.generateToken(authentication);
    }

    public void createUserWith(String username, String password) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        Optional<Role> roles = roleRepository.findByName("USER");
        roles.ifPresent(role -> user.setRoles(Collections.singletonList(role)));
        userRepository.save(user);
    }

    public boolean isTokenExpiredOrValid(String accessToken){
       return jwtGenerator.isTokenExpiredOrValid(accessToken);
    }

    public String generateNewToken(String refreshToken){
        String username = refreshTokenRepository.findByRefreshToken(refreshToken).get().getUser().getUsername();
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return jwtGenerator.generateToken(userDetails);
    }

}
