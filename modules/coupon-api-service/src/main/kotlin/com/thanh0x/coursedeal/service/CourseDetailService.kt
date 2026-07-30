package com.thanh0x.coursedeal.service

import com.thanh0x.coursedeal.config.logger
import com.thanh0x.coursedeal.dto.CourseDetailDTO
import com.thanh0x.coursedeal.dto.CourseReviewsDTO
import com.thanh0x.coursedeal.dto.CurriculumDTO
import com.thanh0x.coursedeal.dto.RelatedCourseDTO
import com.thanh0x.coursedeal.exception.ResourceNotFoundException
import com.thanh0x.coursedeal.model.coupon.CouponCourseData
import com.thanh0x.coursedeal.repository.CouponCourseRepository
import org.json.JSONObject
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

/**
 * Service for fetching and transforming comprehensive course details from Udemy API.
 * Includes caching to reduce external API calls. Parsing of each response section is
 * delegated to dedicated parsers to keep this class focused on orchestration.
 */
@Service
class CourseDetailService(
    private val externalCourseApiClient: ExternalCourseApiClient,
    private val couponCourseRepository: CouponCourseRepository,
    private val curriculumParser: CurriculumParser,
    private val reviewParser: ReviewParser,
    private val relatedCourseParser: RelatedCourseParser,
    private val extrasParser: CourseDetailExtrasParser,
) {
    private val log = logger()

    /**
     * Gets comprehensive course details including reviews, curriculum, and related courses.
     * Results are cached for 24 hours.
     *
     * @param courseId the course ID
     * @param couponCode optional coupon code
     * @return CourseDetailDTO with all course information
     */
    @Cacheable(
        value = ["courseDetails"],
        key = "#courseId + '_' + (#couponCode != null ? #couponCode : 'none')",
        unless = "#result == null",
    )
    fun getCourseDetails(
        courseId: Int,
        couponCode: String?,
    ): CourseDetailDTO? {
        log.info("Fetching course details for courseId: {}, couponCode: {}", courseId, couponCode)

        val courseData =
            couponCourseRepository.findByCourseId(courseId)
                ?: throw ResourceNotFoundException("Course not found in database for courseId: $courseId")

        val landingComponents = externalCourseApiClient.getCourseLandingComponentsJson(courseId, couponCode)
        val reviewsResponse = externalCourseApiClient.getCourseReviewsJson(courseId, 1)
        val relatedCoursesResponse = externalCourseApiClient.getRelatedCoursesJson(courseId)

        val startPreviewId = resolveStartPreviewId(courseData.previewVideo, landingComponents)
        val previewPageJson = startPreviewId?.let { externalCourseApiClient.getPreviewPageJson(courseId, it) }

        val (previewVideoUrl, previewImageUrl) = resolvePreviewAssets(courseData, landingComponents)

        val dto =
            CourseDetailDTO(
                courseId = courseData.courseId,
                title = courseData.title,
                heading = courseData.heading,
                description = courseData.description,
                author = courseData.author,
                category = courseData.category,
                subCategory = courseData.subCategory,
                level = courseData.level,
                language = courseData.language,
                rating = courseData.rating,
                reviews = courseData.reviews,
                students = courseData.students,
                contentLength = courseData.contentLength,
                previewImage = previewImageUrl,
                previewVideo = previewVideoUrl,
                couponUrl = courseData.couponUrl,
                couponCode = courseData.couponCode,
                usesRemaining = courseData.usesRemaining,
                expiredDate = courseData.expiredDate,
            )

        return enrichDto(dto, landingComponents, reviewsResponse, relatedCoursesResponse, previewPageJson)
    }

    private fun resolveStartPreviewId(
        previewVideoPath: String?,
        landingComponents: JSONObject?,
    ): Long? {
        val fromPath =
            previewVideoPath
                ?.takeIf { it.contains("startPreviewId=") }
                ?.substringAfter("startPreviewId=")
                ?.substringBefore("&")
                ?.toLongOrNull()

        return fromPath ?: extractPreviewIdFromLandingComponents(landingComponents)
    }

    private fun extractPreviewIdFromLandingComponents(landingComponents: JSONObject?): Long? =
        landingComponents
            ?.optJSONObject("sidebar_container")
            ?.optJSONObject("componentProps")
            ?.optJSONObject("introductionAsset")
            ?.let { asset ->
                asset.optLong("id", 0).takeIf { it != 0L } ?: asset.optLong("asset_id", 0).takeIf { it != 0L }
            }

    private fun resolvePreviewAssets(
        courseData: CouponCourseData,
        landingComponents: JSONObject?,
    ): Pair<String?, String?> {
        var videoUrl = courseData.previewVideo
        var imageUrl = courseData.previewImage

        val id =
            landingComponents?.optJSONObject("sidebar_container")
                ?.optJSONObject("componentProps")
                ?.optJSONObject("introductionAsset")
                ?.let { it.optLong("id", 0).takeIf { i -> i != 0L } ?: it.optLong("asset_id", 0) } ?: 0L

        if (id > 0) {
            externalCourseApiClient.getAssetJson(id)?.apply {
                optJSONArray("media_sources")?.optJSONObject(0)?.optString("src")?.takeIf { it.isNotEmpty() }?.let {
                    videoUrl = it
                }
                optString("thumbnail_url").takeIf { it.isNotEmpty() }?.let {
                    imageUrl = it
                }
            }
        }

        return Pair(videoUrl, imageUrl)
    }

    private fun enrichDto(
        dto: CourseDetailDTO,
        landingComponents: JSONObject?,
        reviewsResponse: JSONObject?,
        relatedCoursesResponse: JSONObject?,
        previewPageJson: JSONObject?,
    ): CourseDetailDTO {
        var result = dto
        if (landingComponents != null) {
            result =
                result.copy(
                    curriculum = curriculumParser.parse(landingComponents),
                    pricingInfo = extrasParser.parsePricingInfo(landingComponents),
                    incentives = extrasParser.parseIncentives(landingComponents),
                )
        }
        if (reviewsResponse != null) {
            result = result.copy(reviewsSummary = reviewParser.parseSummary(reviewsResponse))
        }
        if (relatedCoursesResponse != null) {
            result = result.copy(relatedCourses = relatedCourseParser.parseAll(relatedCoursesResponse))
        }
        if (previewPageJson != null) {
            result = result.copy(previewVideos = extrasParser.parsePreviewVideos(previewPageJson))
        }
        return result
    }

    /**
     * Gets paginated course reviews.
     * Results are cached for 6 hours.
     *
     * @param courseId the course ID
     * @param page the page number (1-indexed)
     * @return CourseReviewsDTO with paginated reviews
     */
    @Cacheable(value = ["courseReviews"], key = "#courseId + '_' + #page", unless = "#result == null")
    fun getCourseReviews(
        courseId: Int,
        page: Int,
    ): CourseReviewsDTO? {
        log.info("Fetching reviews for courseId: {}, page: {}", courseId, page)

        val reviewsResponse = externalCourseApiClient.getCourseReviewsJson(courseId, page) ?: return null

        return reviewParser.parsePaginated(reviewsResponse, page)
    }

    /**
     * Gets course curriculum/syllabus.
     * Results are cached for 24 hours.
     *
     * @param courseId the course ID
     * @param couponCode optional coupon code
     * @return CurriculumDTO with course sections
     */
    @Cacheable(
        value = ["courseCurriculum"],
        key = "#courseId + '_' + (#couponCode != null ? #couponCode : 'none')",
        unless = "#result == null",
    )
    fun getCourseCurriculum(
        courseId: Int,
        couponCode: String?,
    ): CurriculumDTO? {
        log.info("Fetching curriculum for courseId: {}, couponCode: {}", courseId, couponCode)

        val landingComponents =
            externalCourseApiClient.getCourseLandingComponentsJson(courseId, couponCode)
                ?: return null

        return curriculumParser.parse(landingComponents)
    }

    /**
     * Gets related/recommended courses.
     * Results are cached for 12 hours.
     *
     * @param courseId the course ID
     * @return List of RelatedCourseDTO
     */
    @Cacheable(value = ["relatedCourses"], key = "#courseId", unless = "#result == null")
    fun getRelatedCourses(courseId: Int): List<RelatedCourseDTO> {
        log.info("Fetching related courses for courseId: {}", courseId)

        val relatedCoursesResponse = externalCourseApiClient.getRelatedCoursesJson(courseId) ?: return emptyList()

        return relatedCourseParser.parseAll(relatedCoursesResponse)
    }
}
