package com.thanh0x.coursedeal.controller;

import com.thanh0x.coursedeal.dto.AuthResponseDTO;
import com.thanh0x.coursedeal.service.PasskeyService;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/passkey")
public class PasskeyAuthController {

    private final PasskeyService passkeyService;

    public PasskeyAuthController(PasskeyService passkeyService) {
        this.passkeyService = passkeyService;
    }

    @GetMapping("/registration/options")
    public ResponseEntity<PublicKeyCredentialCreationOptions> getRegistrationOptions(
            @RequestParam String email, 
            @RequestParam(defaultValue = "Course Deal User") String name) {
        return ResponseEntity.ok(passkeyService.startRegistration(email, name));
    }

    @PostMapping("/registration/finish")
    public ResponseEntity<AuthResponseDTO> finishRegistration(
            @RequestParam String email, 
            @RequestBody String responseJson) {
        return ResponseEntity.ok(passkeyService.finishRegistration(email, responseJson));
    }

    @GetMapping("/authentication/options")
    public ResponseEntity<AssertionRequest> getAuthenticationOptions(@RequestParam String email) {
        return ResponseEntity.ok(passkeyService.startAuthentication(email));
    }

    @PostMapping("/authentication/finish")
    public ResponseEntity<AuthResponseDTO> finishAuthentication(
            @RequestParam String email, 
            @RequestBody String responseJson) {
        return ResponseEntity.ok(passkeyService.finishAuthentication(email, responseJson));
    }
}
