package com.thanh0x.coursedeal.dto;

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
    private Set<String> categories;
    private Set<String> keywords;
    private boolean notificationsEnabled;
}
