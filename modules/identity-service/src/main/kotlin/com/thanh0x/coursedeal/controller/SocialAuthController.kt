package com.thanh0x.coursedeal.controller

import com.thanh0x.coursedeal.dto.AuthResponseDTO
import com.thanh0x.coursedeal.dto.SocialLoginRequestDTO
import com.thanh0x.coursedeal.service.SocialAuthService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth/social")
class SocialAuthController(private val socialAuthService: SocialAuthService) {
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: SocialLoginRequestDTO,
    ): ResponseEntity<AuthResponseDTO> {
        return ResponseEntity.ok(socialAuthService.login(request))
    }
}
