package com.thanh0x.coursedeal.controller

import com.thanh0x.coursedeal.dto.AuthResponseDTO
import com.thanh0x.coursedeal.service.PasskeyService
import com.yubico.webauthn.AssertionRequest
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth/passkey")
class PasskeyAuthController(private val passkeyService: PasskeyService) {

    @GetMapping("/registration/options")
    fun getRegistrationOptions(
        @RequestParam email: String,
        @RequestParam(defaultValue = "Course Deal User") name: String
    ): ResponseEntity<PublicKeyCredentialCreationOptions> {
        return ResponseEntity.ok(passkeyService.startRegistration(email, name))
    }

    @PostMapping("/registration/finish")
    fun finishRegistration(
        @RequestParam email: String,
        @RequestBody responseJson: String
    ): ResponseEntity<AuthResponseDTO> {
        return ResponseEntity.ok(passkeyService.finishRegistration(email, responseJson))
    }

    @GetMapping("/authentication/options")
    fun getAuthenticationOptions(@RequestParam email: String): ResponseEntity<AssertionRequest> {
        return ResponseEntity.ok(passkeyService.startAuthentication(email))
    }

    @PostMapping("/authentication/finish")
    fun finishAuthentication(
        @RequestParam email: String,
        @RequestBody responseJson: String
    ): ResponseEntity<AuthResponseDTO> {
        return ResponseEntity.ok(passkeyService.finishAuthentication(email, responseJson))
    }
}
