package com.thanh0x.coursedeal.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO for user preferences.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferenceDTO {
    @NotNull(message = "Categories set cannot be null")
    private Set<String> categories;

    @NotNull(message = "Keywords set cannot be null")
    private Set<String> keywords;

    private boolean notificationsEnabled;
}
