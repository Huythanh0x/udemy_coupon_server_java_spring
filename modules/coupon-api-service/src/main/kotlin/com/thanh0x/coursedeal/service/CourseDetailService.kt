package com.thanh0x.coursedeal.service

import com.thanh0x.coursedeal.dto.*
import com.thanh0x.coursedeal.exception.ResourceNotFoundException
import com.thanh0x.coursedeal.repository.CouponCourseRepository
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

/**
 * Service for fetching and transforming comprehensive course details from Udemy API.
 * Includes caching to reduce external API calls.
 */
@Service
class CourseDetailService(
    private val externalCourseApiClient: ExternalCourseApiClient,
    private val couponCourseRepository: CouponCourseRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

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
        unless = "#result == null"
    )
    fun getCourseDetails(courseId: Int, couponCode: String?): CourseDetailDTO? {
        log.info("Fetching course details for courseId: {}, couponCode: {}", courseId, couponCode)

        // Get basic course data from database
        val courseData = couponCourseRepository.findByCourseId(courseId)
            ?: throw ResourceNotFoundException("Course not found in database for courseId: $courseId")

        // Fetch additional details from Udemy API
        val landingComponents = externalCourseApiClient.getCourseLandingComponentsJson(courseId, couponCode)
        val reviewsResponse = externalCourseApiClient.getCourseReviewsJson(courseId, 1)
        val relatedCoursesResponse = externalCourseApiClient.getRelatedCoursesJson(courseId)

        // Extract startPreviewId from previewVideo URL or landing components
        var startPreviewId: Long? = null
        val previewVideoPath = courseData.previewVideo
        if (previewVideoPath != null && previewVideoPath.contains("startPreviewId=")) {
            try {
                val parts = previewVideoPath.split("startPreviewId=")
                if (parts.size > 1) {
                    val idPart = parts[1].split("&")[0]
                    startPreviewId = idPart.toLong()
                }
            } catch (e: Exception) {
                log.debug("Could not extract startPreviewId from previewVideo path: {}", previewVideoPath)
            }
        }

        // If not found in previewVideo path, try to get from landing components
        if (startPreviewId == null && landingComponents != null) {
            try {
                val sidebarContainer = landingComponents.optJSONObject("sidebar_container")
                if (sidebarContainer != null) {
                    val componentProps = sidebarContainer.optJSONObject("componentProps")
                    if (componentProps != null) {
                        val introductionAsset = componentProps.optJSONObject("introductionAsset")
                        if (introductionAsset != null) {
                            startPreviewId = introductionAsset.optLong("id", 0)
                            if (startPreviewId == 0L) {
                                startPreviewId = introductionAsset.optLong("asset_id", 0)
                            }
                            if (startPreviewId == 0L) {
                                startPreviewId = null
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                log.debug("Could not extract startPreviewId from landing components", e)
            }
        }

        // Fetch preview page to get all preview videos
        var previewPageJson: JSONObject? = null
        if (startPreviewId != null) {
            previewPageJson = externalCourseApiClient.getPreviewPageJson(courseId, startPreviewId)
        }

        // Derive preview video and image from Udemy asset API when possible
        var previewVideoUrl = courseData.previewVideo
        var previewImageUrl = courseData.previewImage

        if (landingComponents != null) {
            try {
                val sidebarContainer = landingComponents.optJSONObject("sidebar_container")
                if (sidebarContainer != null) {
                    val componentProps = sidebarContainer.optJSONObject("componentProps")
                    if (componentProps != null) {
                        val introductionAsset = componentProps.optJSONObject("introductionAsset")
                        if (introductionAsset != null) {
                            var assetId = introductionAsset.optLong("id", 0)
                            if (assetId == 0L) {
                                assetId = introductionAsset.optLong("asset_id", 0)
                            }

                            if (assetId > 0) {
                                val assetJson = externalCourseApiClient.getAssetJson(assetId)
                                if (assetJson != null) {
                                    // Prefer the first media source (usually HLS m3u8)
                                    val mediaSources = assetJson.optJSONArray("media_sources")
                                    if (mediaSources != null && mediaSources.length() > 0) {
                                        val firstSource = mediaSources.optJSONObject(0)
                                        if (firstSource != null) {
                                            val src = firstSource.optString("src", "")
                                            if (src.isNotEmpty()) {
                                                previewVideoUrl = src
                                            }
                                        }
                                    }

                                    val thumbnail = assetJson.optString("thumbnail_url", "")
                                    if (thumbnail.isNotEmpty()) {
                                        previewImageUrl = thumbnail
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                log.warn("Failed to extract preview asset from Udemy API, falling back to database values", e)
            }
        }

        // Build DTO
        val dto = CourseDetailDTO(
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
            expiredDate = courseData.expiredDate
        )

        // Parse and add extra info
        val result = if (landingComponents != null) {
            dto.copy(
                curriculum = parseCurriculum(landingComponents),
                pricingInfo = parsePricingInfo(landingComponents),
                incentives = parseIncentives(landingComponents)
            )
        } else dto

        val resultWithReviews = if (reviewsResponse != null) {
            result.copy(reviewsSummary = parseReviewsSummary(reviewsResponse))
        } else result

        val resultWithRelated = if (relatedCoursesResponse != null) {
            resultWithReviews.copy(relatedCourses = parseRelatedCourses(relatedCoursesResponse))
        } else resultWithReviews

        return if (previewPageJson != null) {
            resultWithRelated.copy(previewVideos = parsePreviewVideos(previewPageJson))
        } else resultWithRelated
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
    fun getCourseReviews(courseId: Int, page: Int): CourseReviewsDTO? {
        log.info("Fetching reviews for courseId: {}, page: {}", courseId, page)

        val reviewsResponse = externalCourseApiClient.getCourseReviewsJson(courseId, page) ?: return null

        return parseCourseReviews(reviewsResponse, page)
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
        unless = "#result == null"
    )
    fun getCourseCurriculum(courseId: Int, couponCode: String?): CurriculumDTO? {
        log.info("Fetching curriculum for courseId: {}, couponCode: {}", courseId, couponCode)

        val landingComponents = externalCourseApiClient.getCourseLandingComponentsJson(courseId, couponCode) ?: return null

        return parseCurriculum(landingComponents)
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

        return parseRelatedCourses(relatedCoursesResponse)
    }

    // ========== Private parsing methods ==========

    private fun parseCurriculum(landingComponents: JSONObject): CurriculumDTO? {
        try {
            val curriculumContext = landingComponents.optJSONObject("curriculum_context") ?: return null
            val data = curriculumContext.optJSONObject("data") ?: return null

            val sectionsArray = data.optJSONArray("sections")
            val sections = mutableListOf<CurriculumSectionDTO>()

            if (sectionsArray != null) {
                for (i in 0 until sectionsArray.length()) {
                    val sectionObj = sectionsArray.optJSONObject(i)
                    if (sectionObj != null) {
                        sections.add(parseCurriculumSection(sectionObj))
                    }
                }
            }

            return CurriculumDTO(
                sections = sections,
                totalDuration = data.optString("estimated_content_length_text", ""),
                totalDurationSeconds = data.optInt("estimated_content_length_in_seconds", 0),
                totalLectures = data.optInt("num_of_published_lectures", 0)
            )
        } catch (e: Exception) {
            log.error("Error parsing curriculum", e)
            return null
        }
    }

    private fun parseCurriculumSection(sectionObj: JSONObject): CurriculumSectionDTO {
        val itemsArray = sectionObj.optJSONArray("items")
        val items = mutableListOf<CurriculumItemDTO>()

        if (itemsArray != null) {
            for (i in 0 until itemsArray.length()) {
                val itemObj = itemsArray.optJSONObject(i)
                if (itemObj != null) {
                    items.add(parseCurriculumItem(itemObj))
                }
            }
        }

        return CurriculumSectionDTO(
            title = sectionObj.optString("title", ""),
            index = sectionObj.optInt("index", 0),
            duration = sectionObj.optString("content_length_text", ""),
            durationSeconds = sectionObj.optInt("content_length", 0),
            lectureCount = sectionObj.optInt("lecture_count", 0),
            items = items
        )
    }

    private fun parseCurriculumItem(itemObj: JSONObject): CurriculumItemDTO {
        return CurriculumItemDTO(
            id = itemObj.optLong("id", 0),
            title = itemObj.optString("title", ""),
            description = itemObj.optString("description", ""),
            contentSummary = itemObj.optString("content_summary", ""),
            itemType = itemObj.optString("item_type", ""),
            canBePreviewed = itemObj.optBoolean("can_be_previewed", false),
            isCodingExercise = itemObj.optBoolean("is_coding_exercise", false),
            isPracticeTest = itemObj.optBoolean("is_practice_test", false),
            previewUrl = itemObj.optString("preview_url", ""),
            learnUrl = itemObj.optString("learn_url", ""),
            objectIndex = itemObj.optInt("object_index", 0)
        )
    }

    private fun parseReviewsSummary(reviewsResponse: JSONObject): ReviewsSummaryDTO? {
        try {
            val results = reviewsResponse.optJSONArray("results")
            val recentReviews = mutableListOf<ReviewDTO>()

            if (results != null) {
                val maxReviews = Math.min(5, results.length())
                for (i in 0 until maxReviews) {
                    val reviewObj = results.optJSONObject(i)
                    if (reviewObj != null) {
                        recentReviews.add(parseReview(reviewObj))
                    }
                }
            }

            return ReviewsSummaryDTO(
                totalCount = reviewsResponse.optInt("count", 0),
                averageRating = null, // Calculate from reviews if needed
                recentReviews = recentReviews
            )
        } catch (e: Exception) {
            log.error("Error parsing reviews summary", e)
            return null
        }
    }

    private fun parseCourseReviews(reviewsResponse: JSONObject, page: Int): CourseReviewsDTO? {
        try {
            val results = reviewsResponse.optJSONArray("results")
            val reviews = mutableListOf<ReviewDTO>()

            if (results != null) {
                for (i in 0 until results.length()) {
                    val reviewObj = results.optJSONObject(i)
                    if (reviewObj != null) {
                        reviews.add(parseReview(reviewObj))
                    }
                }
            }

            return CourseReviewsDTO(
                reviews = reviews,
                totalCount = reviewsResponse.optInt("count", 0),
                currentPage = page,
                hasNext = reviewsResponse.optString("next", null) != null,
                hasPrevious = reviewsResponse.optString("previous", null) != null,
                nextUrl = reviewsResponse.optString("next", null),
                previousUrl = reviewsResponse.optString("previous", null)
            )
        } catch (e: Exception) {
            log.error("Error parsing course reviews", e)
            return null
        }
    }

    private fun parseReview(reviewObj: JSONObject): ReviewDTO {
        val userObj = reviewObj.optJSONObject("user")
        val user = userObj?.let {
            ReviewUserDTO(
                displayName = it.optString("display_name", ""),
                publicDisplayName = it.optString("public_display_name", ""),
                image50x50 = it.optString("image_50x50", ""),
                initials = it.optString("initials", "")
            )
        }

        val responseObj = reviewObj.optJSONObject("response")
        val response = responseObj?.let {
            val responseUserObj = it.optJSONObject("user")
            val responseUser = responseUserObj?.let { ru ->
                ReviewUserDTO(
                    displayName = ru.optString("display_name", ""),
                    publicDisplayName = ru.optString("public_display_name", ""),
                    image50x50 = ru.optString("image_50x50", ""),
                    initials = ru.optString("initials", "")
                )
            }

            ReviewResponseDTO(
                content = it.optString("content", ""),
                contentHtml = it.optString("content_html", ""),
                created = it.optString("created", ""),
                createdFormatted = it.optString("created_formatted_with_time_since", ""),
                user = responseUser
            )
        }

        return ReviewDTO(
            id = reviewObj.optLong("id", 0),
            content = reviewObj.optString("content", ""),
            contentHtml = reviewObj.optString("content_html", ""),
            rating = reviewObj.optDouble("rating", 0.0).toFloat(),
            created = reviewObj.optString("created", ""),
            createdFormatted = reviewObj.optString("created_formatted_with_time_since", ""),
            user = user,
            response = response
        )
    }

    private fun parseRelatedCourses(relatedCoursesResponse: JSONObject): List<RelatedCourseDTO> {
        val relatedCourses = mutableListOf<RelatedCourseDTO>()

        try {
            val units = relatedCoursesResponse.optJSONArray("units")
            if (units != null && units.length() > 0) {
                val firstUnit = units.optJSONObject(0)
                if (firstUnit != null) {
                    val items = firstUnit.optJSONArray("items")
                    if (items != null) {
                        for (i in 0 until items.length()) {
                            val courseObj = items.optJSONObject(i)
                            if (courseObj != null) {
                                parseRelatedCourse(courseObj)?.let {
                                    relatedCourses.add(it)
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            log.error("Error parsing related courses", e)
        }

        return relatedCourses
    }

    private fun parseRelatedCourse(courseObj: JSONObject): RelatedCourseDTO? {
        try {
            val instructors = courseObj.optJSONArray("visible_instructors")
            var author = "Unknown"
            if (instructors != null && instructors.length() > 0) {
                val instructor = instructors.optJSONObject(0)
                if (instructor != null) {
                    author = instructor.optString("display_name", "Unknown")
                }
            }

            return RelatedCourseDTO(
                courseId = courseObj.optInt("id", 0),
                title = courseObj.optString("title", ""),
                headline = courseObj.optString("headline", ""),
                url = "https://www.udemy.com" + courseObj.optString("url", ""),
                image240x135 = courseObj.optString("image_240x135", ""),
                image480x270 = courseObj.optString("image_480x270", ""),
                image750x422 = courseObj.optString("image_750x422", ""),
                author = author,
                rating = courseObj.optDouble("rating", 0.0).toFloat(),
                numReviews = courseObj.optInt("num_reviews", 0),
                numSubscribers = courseObj.optInt("num_subscribers", 0),
                contentInfo = courseObj.optString("content_info_short", ""),
                instructionalLevel = courseObj.optString("instructional_level_simple", "")
            )
        } catch (e: Exception) {
            log.error("Error parsing related course", e)
            return null
        }
    }

    private fun parsePricingInfo(landingComponents: JSONObject): PricingInfoDTO? {
        try {
            val priceText = landingComponents.optJSONObject("price_text") ?: return null
            val data = priceText.optJSONObject("data") ?: return null
            val pricingResult = data.optJSONObject("pricing_result") ?: return null

            val price = pricingResult.optJSONObject("price")
            val listPrice = pricingResult.optJSONObject("list_price")
            val savingPrice = pricingResult.optJSONObject("saving_price")
            val campaign = pricingResult.optJSONObject("campaign")

            val discountExpiration = landingComponents.optJSONObject("discount_expiration")
            var discountDeadlineText: String? = null
            if (discountExpiration != null) {
                val discountData = discountExpiration.optJSONObject("data")
                if (discountData != null) {
                    discountDeadlineText = discountData.optString("discount_deadline_text", null)
                }
            }

            return PricingInfoDTO(
                price = price?.optDouble("amount", 0.0)?.toFloat(),
                listPrice = listPrice?.optDouble("amount", 0.0)?.toFloat(),
                savingPrice = savingPrice?.optDouble("amount", 0.0)?.toFloat(),
                currency = price?.optString("currency", "") ?: "",
                priceString = price?.optString("price_string", "") ?: "",
                currencySymbol = price?.optString("currency_symbol", "") ?: "",
                discountPercent = pricingResult.optInt("discount_percent_for_display", 0),
                discountDeadlineText = discountDeadlineText,
                couponCode = pricingResult.optString("code", ""),
                usesRemaining = campaign?.optInt("uses_remaining", 0),
                maximumUses = campaign?.optInt("maximum_uses", 0)
            )
        } catch (e: Exception) {
            log.error("Error parsing pricing info", e)
            return null
        }
    }

    private fun parseIncentives(landingComponents: JSONObject): IncentivesDTO? {
        try {
            val incentives = landingComponents.optJSONObject("incentives") ?: return null

            return IncentivesDTO(
                videoContentLength = incentives.optString("video_content_length", ""),
                numArticles = incentives.optInt("num_articles", 0),
                numQuizzes = incentives.optInt("num_quizzes", 0),
                numPracticeTests = incentives.optInt("num_practice_tests", 0),
                numCodingExercises = incentives.optInt("num_coding_exercises", 0),
                hasLifetimeAccess = incentives.optBoolean("has_lifetime_access", false),
                devicesAccess = incentives.optString("devices_access", ""),
                hasAssignments = incentives.optBoolean("has_assignments", false),
                hasCertificate = incentives.optBoolean("has_certificate", false),
                hasClosedCaptions = incentives.optBoolean("has_closed_captions", false)
            )
        } catch (e: Exception) {
            log.error("Error parsing incentives", e)
            return null
        }
    }

    private fun parsePreviewVideos(previewPageJson: JSONObject): List<PreviewVideoDTO> {
        val previewVideos = mutableListOf<PreviewVideoDTO>()

        try {
            val previews = previewPageJson.optJSONArray("previews")
            if (previews != null) {
                for (i in 0 until previews.length()) {
                    val previewObj = previews.optJSONObject(i)
                    if (previewObj != null) {
                        parsePreviewVideo(previewObj)?.let {
                            previewVideos.add(it)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            log.error("Error parsing preview videos", e)
        }

        return previewVideos
    }

    private fun parsePreviewVideo(previewObj: JSONObject): PreviewVideoDTO? {
        try {
            // Extract video URL from media_sources (HLS m3u8)
            var videoUrl: String? = null
            val mediaSources = previewObj.optJSONArray("media_sources")
            if (mediaSources != null && mediaSources.length() > 0) {
                val firstSource = mediaSources.optJSONObject(0)
                if (firstSource != null) {
                    videoUrl = firstSource.optString("src", "")
                }
            }

            // Extract stream URLs (MP4 files at different resolutions)
            val streamUrls = mutableListOf<VideoSourceDTO>()
            val streamUrlsObj = previewObj.optJSONObject("stream_urls")
            if (streamUrlsObj != null) {
                val videoStreams = streamUrlsObj.optJSONArray("Video")
                if (videoStreams != null) {
                    for (i in 0 until videoStreams.length()) {
                        val streamObj = videoStreams.optJSONObject(i)
                        if (streamObj != null) {
                            val source = VideoSourceDTO(
                                type = streamObj.optString("type", ""),
                                label = streamObj.optString("label", ""),
                                file = streamObj.optString("file", "")
                            )
                            if (source.file?.isNotEmpty() == true) {
                                streamUrls.add(source)
                            }
                        }
                    }
                }
            }

            return PreviewVideoDTO(
                id = previewObj.optLong("id", 0),
                title = previewObj.optString("title", ""),
                thumbnailUrl = previewObj.optString("thumbnail_url", ""),
                contentSummary = previewObj.optString("content_summary", ""),
                timeEstimation = previewObj.optInt("time_estimation", 0),
                videoUrl = videoUrl,
                streamUrls = if (streamUrls.isEmpty()) null else streamUrls
            )
        } catch (e: Exception) {
            log.error("Error parsing preview video", e)
            return null
        }
    }
}
