package com.huythanh0x.udemycoupons.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for a preview video from Udemy course preview page.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreviewVideoDTO {
    private Long id;
    private String title;
    private String thumbnailUrl;
    private String contentSummary; // e.g., "02:22"
    private Integer timeEstimation; // in seconds
    private String videoUrl; // HLS m3u8 URL from media_sources
    private List<VideoSourceDTO> streamUrls; // MP4 files at different resolutions
}

