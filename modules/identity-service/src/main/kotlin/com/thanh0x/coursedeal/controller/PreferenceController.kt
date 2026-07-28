package com.thanh0x.coursedeal.controller

import com.thanh0x.coursedeal.dto.PreferenceDTO
import com.thanh0x.coursedeal.model.user.UserEntity
import com.thanh0x.coursedeal.model.user.UserPreference
import com.thanh0x.coursedeal.repository.UserPreferenceRepository
import com.thanh0x.coursedeal.repository.UserRepository
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/preferences")
class PreferenceController(
    private val userPreferenceRepository: UserPreferenceRepository,
    private val userRepository: UserRepository,
) {
    @GetMapping
    fun getPreferences(
        @AuthenticationPrincipal user: UserEntity,
    ): ResponseEntity<PreferenceDTO> {
        val preference =
            userPreferenceRepository.findById(user.id)
                .orElseGet { createDefaultPreference(user) }

        return ResponseEntity.ok(mapToDto(preference))
    }

    @PutMapping
    fun updatePreferences(
        @AuthenticationPrincipal user: UserEntity,
        @Valid @RequestBody dto: PreferenceDTO,
    ): ResponseEntity<PreferenceDTO> {
        val preference =
            userPreferenceRepository.findById(user.id)
                .orElseGet { createDefaultPreference(user) }

        preference.categories = dto.categories.toMutableSet()
        preference.keywords = dto.keywords.toMutableSet()
        preference.notificationsEnabled = dto.notificationsEnabled

        val saved = userPreferenceRepository.save(preference)
        return ResponseEntity.ok(mapToDto(saved))
    }

    private fun createDefaultPreference(user: UserEntity): UserPreference {
        val pref =
            UserPreference(
                user = user,
                userId = user.id,
                notificationsEnabled = true,
            )
        return userPreferenceRepository.save(pref)
    }

    private fun mapToDto(pref: UserPreference): PreferenceDTO {
        return PreferenceDTO(
            categories = pref.categories,
            keywords = pref.keywords,
            notificationsEnabled = pref.notificationsEnabled,
        )
    }
}
