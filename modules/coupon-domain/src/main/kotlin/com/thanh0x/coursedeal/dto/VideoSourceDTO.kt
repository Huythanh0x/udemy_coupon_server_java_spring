package com.thanh0x.coursedeal.dto

/**
 * DTO for a video source (MP4 file at specific resolution).
 */
data class VideoSourceDTO(
    val type: String? = null, // e.g., "video/mp4"
    val label: String? = null, // e.g., "720", "480", "360", "144", "Auto"
    val file: String? = null // URL to the video file
)
