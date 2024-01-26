package com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.security;

import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.utils.Constant;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, response.getHeader(Constant.AUTHENTICATION_EXCEPTION_HEADER));
    }
}
