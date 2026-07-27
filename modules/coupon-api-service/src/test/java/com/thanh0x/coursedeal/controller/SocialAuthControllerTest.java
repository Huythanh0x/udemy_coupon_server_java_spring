package com.thanh0x.coursedeal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanh0x.coursedeal.dto.AuthResponseDTO;
import com.thanh0x.coursedeal.dto.SocialLoginRequestDTO;
import com.thanh0x.coursedeal.model.user.AuthProvider;
import com.thanh0x.coursedeal.service.SocialAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SocialAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SocialAuthService socialAuthService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_ShouldReturnToken() throws Exception {
        SocialLoginRequestDTO request = new SocialLoginRequestDTO();
        request.setProvider(AuthProvider.GOOGLE);
        request.setIdToken("mock-google-token");

        AuthResponseDTO response = AuthResponseDTO.builder()
                .accessToken("mock-jwt-token")
                .tokenType("Bearer")
                .build();

        when(socialAuthService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/social/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock-jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }
}
