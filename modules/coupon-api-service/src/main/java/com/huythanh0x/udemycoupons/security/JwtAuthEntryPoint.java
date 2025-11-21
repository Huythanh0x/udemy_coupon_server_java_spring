package com.huythanh0x.udemycoupons.security;

import com.huythanh0x.udemycoupons.utils.Constant;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * This class represents the entry point for handling JWT authentication errors.
 * It implements the AuthenticationEntryPoint interface and overrides the commence method.
 */
@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {
    /**
     * Override method to handle authentication exceptions.
     * Sends an unauthorized response with authentication exception header.
     *
     * @param request servlet request
     * @param response servlet response
     * @param authException authentication exception that occurred
     * @throws IOException if an I/O error occurs while sending the response error
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, response.getHeader(Constant.AUTHENTICATION_EXCEPTION_HEADER));
    }
}
