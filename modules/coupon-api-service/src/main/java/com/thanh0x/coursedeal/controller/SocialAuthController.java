package com.thanh0x.coursedeal.controller;

import com.thanh0x.coursedeal.dto.AuthResponseDTO;
import com.thanh0x.coursedeal.dto.SocialLoginRequestDTO;
import com.thanh0x.coursedeal.service.SocialAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/social")
public class SocialAuthController {

    private final SocialAuthService socialAuthService;

    public SocialAuthController(SocialAuthService socialAuthService) {
        this.socialAuthService = socialAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody SocialLoginRequestDTO request) {
        return ResponseEntity.ok(socialAuthService.login(request));
    }
}
