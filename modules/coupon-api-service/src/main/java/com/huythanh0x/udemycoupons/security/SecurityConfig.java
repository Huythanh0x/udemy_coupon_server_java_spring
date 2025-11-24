package com.huythanh0x.udemycoupons.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Configures the security settings for the application.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthEntryPoint authEntryPoint;
    private final CustomUserDetailsService userDetailsService;

    @Autowired
    public SecurityConfig(CustomUserDetailsService userDetailsService, JwtAuthEntryPoint authEntryPoint) {
        this.userDetailsService = userDetailsService;
        this.authEntryPoint = authEntryPoint;
    }

    /**
     * Main security filter chain for all non-actuator endpoints.
     * This has a higher order than ActuatorSecurityConfig (which is @Order(0))
     * so that actuator-specific requests are handled by the actuator chain first.
     * The "any request" matcher requires this chain to be defined last (highest order).
     *
     * @param http The HttpSecurity object used to configure security settings.
     * @return The SecurityFilterChain object for managing security filters.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    @Order(100)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        RequestMatcher nonActuatorMatcher = request -> !request.getRequestURI().startsWith("/actuator/");
        
        http
                .securityMatcher(nonActuatorMatcher)
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/coupons/**").permitAll()
                        .requestMatchers("/*").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(AbstractHttpConfigurer::disable);
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Creates and returns an AuthenticationManager object based on the provided AuthenticationConfiguration.
     *
     * @param authenticationConfiguration the AuthenticationConfiguration used to configure the AuthenticationManager
     * @return the created AuthenticationManager object
     * @throws Exception if an error occurs during the creation of the AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Creates a new instance of BCryptPasswordEncoder to be used as a PasswordEncoder.
     * This bean is used for encoding passwords securely using the BCrypt hashing algorithm.
     *
     * @return a PasswordEncoder instance using the BCrypt algorithm
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Creates a new instance of JWTAuthenticationFilter and returns it as a Spring bean.
     * This filter is responsible for authenticating incoming JSON web tokens (JWT) in the request headers.
     * @return a new JWTAuthenticationFilter object
     */
    @Bean
    public JWTAuthenticationFilter jwtAuthenticationFilter() {
        return new JWTAuthenticationFilter();
    }
}
