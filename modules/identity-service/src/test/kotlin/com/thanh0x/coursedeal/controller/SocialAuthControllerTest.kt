package com.thanh0x.coursedeal.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.thanh0x.coursedeal.dto.AuthResponseDTO
import com.thanh0x.coursedeal.dto.SocialLoginRequestDTO
import com.thanh0x.coursedeal.model.user.AuthProvider
import com.thanh0x.coursedeal.service.SocialAuthService
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
// identity-service has no SecurityConfig of its own (it's owned by coupon-api-service, which
// depends on this module, not the reverse), so Spring Security's default deny-all would 403
// every request here. Filters are irrelevant to what this test verifies (controller <-> mocked
// service wiring), so they're disabled rather than duplicating production security rules.
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class SocialAuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var socialAuthService: SocialAuthService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun login_ShouldReturnToken() {
        val request = SocialLoginRequestDTO(
            provider = AuthProvider.GOOGLE,
            idToken = "mock-google-token"
        )

        val response = AuthResponseDTO(
            accessToken = "mock-jwt-token",
            tokenType = "Bearer"
        )

        `when`(socialAuthService.login(any())).thenReturn(response)

        mockMvc.perform(
            post("/api/v1/auth/social/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value("mock-jwt-token"))
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
    }

    private fun <T> any(): T = ArgumentMatchers.any()
}
