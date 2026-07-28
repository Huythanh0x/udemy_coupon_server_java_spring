package com.thanh0x.coursedeal.dto

/**
 * DTO for a preview video from Udemy course preview page.
 */
data class PreviewVideoDTO(
    val id: Long? = null,
    val title: String? = null,
    val thumbnailUrl: String? = null,
    val contentSummary: String? = null, // e.g., "02:22"
    val timeEstimation: Int? = null, // in seconds
    val videoUrl: String? = null, // HLS m3u8 URL from media_sources
    val streamUrls: List<VideoSourceDTO>? = null // MP4 files at different resolutions
)
