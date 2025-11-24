package com.huythanh0x.udemycoupons.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration securing actuator endpoints with Basic Auth outside of local profile.
 */
public class ActuatorSecurityConfig {

    @Configuration
    @Order(0)
    @Profile("!local")
    static class SecuredActuatorSecurityConfiguration {

        @Value("${prometheus.actuator.username:prometheus}")
        private String actuatorUsername;

        @Value("${prometheus.actuator.password:prometheus}")
        private String actuatorPassword;

        @Bean
        SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
            http.securityMatcher("/actuator/**")
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .httpBasic(Customizer.withDefaults())
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .csrf(AbstractHttpConfigurer::disable)
                    .authenticationProvider(actuatorAuthenticationProvider());
            return http.build();
        }

        private AuthenticationProvider actuatorAuthenticationProvider() {
            return new AuthenticationProvider() {
                @Override
                public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                    String principal = authentication.getName();
                    String credentials = authentication.getCredentials().toString();
                    if (actuatorUsername.equals(principal) && actuatorPassword.equals(credentials)) {
                        return new UsernamePasswordAuthenticationToken(
                                principal,
                                credentials,
                                AuthorityUtils.createAuthorityList("ROLE_ACTUATOR"));
                    }
                    throw new BadCredentialsException("Invalid actuator credentials");
                }

                @Override
                public boolean supports(Class<?> authentication) {
                    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
                }
            };
        }
    }

    @Configuration
    @Order(0)
    @Profile("local")
    static class LocalActuatorSecurityConfiguration {

        @Bean
        SecurityFilterChain actuatorSecurityPermitAll(HttpSecurity http) throws Exception {
            http.securityMatcher("/actuator/**")
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .csrf(AbstractHttpConfigurer::disable);
            return http.build();
        }
    }
}


