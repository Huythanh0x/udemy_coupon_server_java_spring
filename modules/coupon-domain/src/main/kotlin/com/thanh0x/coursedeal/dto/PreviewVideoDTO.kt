package com.thanh0x.coursedeal.dto

/**
 * DTO for a preview video from Udemy course preview page.
 */
data class PreviewVideoDTO(
    val id: Long? = null,
    val title: String? = null,
    val thumbnailUrl: String? = null,
    // e.g., "02:22"
    val contentSummary: String? = null,
    // in seconds
    val timeEstimation: Int? = null,
    // HLS m3u8 URL from media_sources
    val videoUrl: String? = null,
    // MP4 files at different resolutions
    val streamUrls: List<VideoSourceDTO>? = null,
)
