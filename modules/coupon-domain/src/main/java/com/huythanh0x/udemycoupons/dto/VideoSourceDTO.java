package com.huythanh0x.udemycoupons.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for a video source (MP4 file at specific resolution).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoSourceDTO {
    private String type; // e.g., "video/mp4"
    private String label; // e.g., "720", "480", "360", "144", "Auto"
    private String file; // URL to the video file
}

