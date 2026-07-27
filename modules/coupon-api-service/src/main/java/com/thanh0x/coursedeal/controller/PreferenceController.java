package com.thanh0x.coursedeal.controller;

import com.thanh0x.coursedeal.dto.PreferenceDTO;
import com.thanh0x.coursedeal.model.user.UserEntity;
import com.thanh0x.coursedeal.model.user.UserPreference;
import com.thanh0x.coursedeal.repository.UserPreferenceRepository;
import com.thanh0x.coursedeal.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/preferences")
public class PreferenceController {

    private final UserPreferenceRepository userPreferenceRepository;
    private final UserRepository userRepository;

    public PreferenceController(UserPreferenceRepository userPreferenceRepository, UserRepository userRepository) {
        this.userPreferenceRepository = userPreferenceRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<PreferenceDTO> getPreferences(@AuthenticationPrincipal UserEntity user) {
        UserPreference preference = userPreferenceRepository.findById(user.getId())
                .orElseGet(() -> createDefaultPreference(user));
        
        return ResponseEntity.ok(mapToDto(preference));
    }

    @PutMapping
    public ResponseEntity<PreferenceDTO> updatePreferences(@AuthenticationPrincipal UserEntity user, @RequestBody PreferenceDTO dto) {
        UserPreference preference = userPreferenceRepository.findById(user.getId())
                .orElseGet(() -> createDefaultPreference(user));
        
        preference.setCategories(dto.getCategories());
        preference.setKeywords(dto.getKeywords());
        preference.setNotificationsEnabled(dto.isNotificationsEnabled());
        
        UserPreference saved = userPreferenceRepository.save(preference);
        return ResponseEntity.ok(mapToDto(saved));
    }

    private UserPreference createDefaultPreference(UserEntity user) {
        UserPreference pref = UserPreference.builder()
                .user(user)
                .userId(user.getId())
                .notificationsEnabled(true)
                .build();
        return userPreferenceRepository.save(pref);
    }

    private PreferenceDTO mapToDto(UserPreference pref) {
        return PreferenceDTO.builder()
                .categories(pref.getCategories())
                .keywords(pref.getKeywords())
                .notificationsEnabled(pref.isNotificationsEnabled())
                .build();
    }
}
