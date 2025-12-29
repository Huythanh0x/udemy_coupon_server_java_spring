package com.huythanh0x.udemycoupons.service;

import com.huythanh0x.udemycoupons.dto.*;
import com.huythanh0x.udemycoupons.model.coupon.CouponCourseData;
import com.huythanh0x.udemycoupons.repository.CouponCourseRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for fetching and transforming comprehensive course details from Udemy API.
 * Includes caching to reduce external API calls.
 */
@Service
public class CourseDetailService {
    private static final Logger log = LoggerFactory.getLogger(CourseDetailService.class);
    
    @Autowired
    private UdemyApiClient udemyApiClient;
    
    @Autowired
    private CouponCourseRepository couponCourseRepository;
    
    /**
     * Gets comprehensive course details including reviews, curriculum, and related courses.
     * Results are cached for 24 hours.
     *
     * @param courseId the course ID
     * @param couponCode optional coupon code
     * @return CourseDetailDTO with all course information
     */
    @Cacheable(value = "courseDetails", key = "#courseId + '_' + (#couponCode != null ? #couponCode : 'none')", unless = "#result == null")
    public CourseDetailDTO getCourseDetails(Integer courseId, String couponCode) {
        log.info("Fetching course details for courseId: {}, couponCode: {}", courseId, couponCode);
        
        // Get basic course data from database
        CouponCourseData courseData = couponCourseRepository.findByCourseId(courseId);
        if (courseData == null) {
            log.warn("Course not found in database for courseId: {}", courseId);
            return null;
        }
        
        // Fetch additional details from Udemy API
        JSONObject landingComponents = udemyApiClient.getCourseLandingComponentsJson(courseId, couponCode);
        JSONObject reviewsResponse = udemyApiClient.getCourseReviewsJson(courseId, 1);
        JSONObject relatedCoursesResponse = udemyApiClient.getRelatedCoursesJson(courseId);
        
        // Extract startPreviewId from previewVideo URL or landing components
        Long startPreviewId = null;
        String previewVideoPath = courseData.getPreviewVideo();
        if (previewVideoPath != null && previewVideoPath.contains("startPreviewId=")) {
            try {
                String[] parts = previewVideoPath.split("startPreviewId=");
                if (parts.length > 1) {
                    String idPart = parts[1].split("&")[0];
                    startPreviewId = Long.parseLong(idPart);
                }
            } catch (Exception e) {
                log.debug("Could not extract startPreviewId from previewVideo path: {}", previewVideoPath);
            }
        }
        
        // If not found in previewVideo path, try to get from landing components
        if (startPreviewId == null && landingComponents != null) {
            try {
                JSONObject sidebarContainer = landingComponents.optJSONObject("sidebar_container");
                if (sidebarContainer != null) {
                    JSONObject componentProps = sidebarContainer.optJSONObject("componentProps");
                    if (componentProps != null) {
                        JSONObject introductionAsset = componentProps.optJSONObject("introductionAsset");
                        if (introductionAsset != null) {
                            startPreviewId = introductionAsset.optLong("id", 0);
                            if (startPreviewId == 0) {
                                startPreviewId = introductionAsset.optLong("asset_id", 0);
                            }
                            if (startPreviewId == 0) {
                                startPreviewId = null;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Could not extract startPreviewId from landing components", e);
            }
        }
        
        // Fetch preview page to get all preview videos
        JSONObject previewPageJson = null;
        if (startPreviewId != null) {
            previewPageJson = udemyApiClient.getPreviewPageJson(courseId, startPreviewId);
        }

        // Derive preview video and image from Udemy asset API when possible
        String previewVideoUrl = courseData.getPreviewVideo();
        String previewImageUrl = courseData.getPreviewImage();

        if (landingComponents != null) {
            try {
                JSONObject sidebarContainer = landingComponents.optJSONObject("sidebar_container");
                if (sidebarContainer != null) {
                    JSONObject componentProps = sidebarContainer.optJSONObject("componentProps");
                    if (componentProps != null) {
                        JSONObject introductionAsset = componentProps.optJSONObject("introductionAsset");
                        if (introductionAsset != null) {
                            long assetId = introductionAsset.optLong("id", 0);
                            if (assetId == 0) {
                                assetId = introductionAsset.optLong("asset_id", 0);
                            }

                            if (assetId > 0) {
                                JSONObject assetJson = udemyApiClient.getAssetJson(assetId);
                                if (assetJson != null) {
                                    // Prefer the first media source (usually HLS m3u8)
                                    JSONArray mediaSources = assetJson.optJSONArray("media_sources");
                                    if (mediaSources != null && mediaSources.length() > 0) {
                                        JSONObject firstSource = mediaSources.optJSONObject(0);
                                        if (firstSource != null) {
                                            String src = firstSource.optString("src", "");
                                            if (!src.isEmpty()) {
                                                previewVideoUrl = src;
                                            }
                                        }
                                    }

                                    String thumbnail = assetJson.optString("thumbnail_url", "");
                                    if (!thumbnail.isEmpty()) {
                                        previewImageUrl = thumbnail;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to extract preview asset from Udemy API, falling back to database values", e);
            }
        }
        
        // Build DTO
        CourseDetailDTO.CourseDetailDTOBuilder builder = CourseDetailDTO.builder()
                .courseId(courseData.getCourseId())
                .title(courseData.getTitle())
                .heading(courseData.getHeading())
                .description(courseData.getDescription())
                .author(courseData.getAuthor())
                .category(courseData.getCategory())
                .subCategory(courseData.getSubCategory())
                .level(courseData.getLevel())
                .language(courseData.getLanguage())
                .rating(courseData.getRating())
                .reviews(courseData.getReviews())
                .students(courseData.getStudents())
                .contentLength(courseData.getContentLength())
                .previewImage(previewImageUrl)
                .previewVideo(previewVideoUrl)
                .couponUrl(courseData.getCouponUrl())
                .couponCode(courseData.getCouponCode())
                .usesRemaining(courseData.getUsesRemaining())
                .expiredDate(courseData.getExpiredDate());
        
        // Parse and add curriculum
        if (landingComponents != null) {
            builder.curriculum(parseCurriculum(landingComponents))
                   .pricingInfo(parsePricingInfo(landingComponents))
                   .incentives(parseIncentives(landingComponents));
        }
        
        // Parse and add reviews summary
        if (reviewsResponse != null) {
            builder.reviewsSummary(parseReviewsSummary(reviewsResponse));
        }
        
        // Parse and add related courses
        if (relatedCoursesResponse != null) {
            builder.relatedCourses(parseRelatedCourses(relatedCoursesResponse));
        }
        
        // Parse and add preview videos
        if (previewPageJson != null) {
            builder.previewVideos(parsePreviewVideos(previewPageJson));
        }
        
        return builder.build();
    }
    
    /**
     * Gets paginated course reviews.
     * Results are cached for 6 hours.
     *
     * @param courseId the course ID
     * @param page the page number (1-indexed)
     * @return CourseReviewsDTO with paginated reviews
     */
    @Cacheable(value = "courseReviews", key = "#courseId + '_' + #page", unless = "#result == null")
    public CourseReviewsDTO getCourseReviews(Integer courseId, int page) {
        log.info("Fetching reviews for courseId: {}, page: {}", courseId, page);
        
        JSONObject reviewsResponse = udemyApiClient.getCourseReviewsJson(courseId, page);
        if (reviewsResponse == null) {
            return null;
        }
        
        return parseCourseReviews(reviewsResponse, page);
    }
    
    /**
     * Gets course curriculum/syllabus.
     * Results are cached for 24 hours.
     *
     * @param courseId the course ID
     * @param couponCode optional coupon code
     * @return CurriculumDTO with course sections
     */
    @Cacheable(value = "courseCurriculum", key = "#courseId + '_' + (#couponCode != null ? #couponCode : 'none')", unless = "#result == null")
    public CurriculumDTO getCourseCurriculum(Integer courseId, String couponCode) {
        log.info("Fetching curriculum for courseId: {}, couponCode: {}", courseId, couponCode);
        
        JSONObject landingComponents = udemyApiClient.getCourseLandingComponentsJson(courseId, couponCode);
        if (landingComponents == null) {
            return null;
        }
        
        return parseCurriculum(landingComponents);
    }
    
    /**
     * Gets related/recommended courses.
     * Results are cached for 12 hours.
     *
     * @param courseId the course ID
     * @return List of RelatedCourseDTO
     */
    @Cacheable(value = "relatedCourses", key = "#courseId", unless = "#result == null")
    public List<RelatedCourseDTO> getRelatedCourses(Integer courseId) {
        log.info("Fetching related courses for courseId: {}", courseId);
        
        JSONObject relatedCoursesResponse = udemyApiClient.getRelatedCoursesJson(courseId);
        if (relatedCoursesResponse == null) {
            return new ArrayList<>();
        }
        
        return parseRelatedCourses(relatedCoursesResponse);
    }
    
    // ========== Private parsing methods ==========
    
    private CurriculumDTO parseCurriculum(JSONObject landingComponents) {
        try {
            JSONObject curriculumContext = landingComponents.optJSONObject("curriculum_context");
            if (curriculumContext == null) {
                return null;
            }
            
            JSONObject data = curriculumContext.optJSONObject("data");
            if (data == null) {
                return null;
            }
            
            JSONArray sectionsArray = data.optJSONArray("sections");
            List<CurriculumSectionDTO> sections = new ArrayList<>();
            
            if (sectionsArray != null) {
                for (int i = 0; i < sectionsArray.length(); i++) {
                    JSONObject sectionObj = sectionsArray.optJSONObject(i);
                    if (sectionObj != null) {
                        sections.add(parseCurriculumSection(sectionObj));
                    }
                }
            }
            
            return CurriculumDTO.builder()
                    .sections(sections)
                    .totalDuration(data.optString("estimated_content_length_text", ""))
                    .totalDurationSeconds(data.optInt("estimated_content_length_in_seconds", 0))
                    .totalLectures(data.optInt("num_of_published_lectures", 0))
                    .build();
        } catch (Exception e) {
            log.error("Error parsing curriculum", e);
            return null;
        }
    }
    
    private CurriculumSectionDTO parseCurriculumSection(JSONObject sectionObj) {
        JSONArray itemsArray = sectionObj.optJSONArray("items");
        List<CurriculumItemDTO> items = new ArrayList<>();
        
        if (itemsArray != null) {
            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject itemObj = itemsArray.optJSONObject(i);
                if (itemObj != null) {
                    items.add(parseCurriculumItem(itemObj));
                }
            }
        }
        
        return CurriculumSectionDTO.builder()
                .title(sectionObj.optString("title", ""))
                .index(sectionObj.optInt("index", 0))
                .duration(sectionObj.optString("content_length_text", ""))
                .durationSeconds(sectionObj.optInt("content_length", 0))
                .lectureCount(sectionObj.optInt("lecture_count", 0))
                .items(items)
                .build();
    }
    
    private CurriculumItemDTO parseCurriculumItem(JSONObject itemObj) {
        return CurriculumItemDTO.builder()
                .id(itemObj.optLong("id", 0))
                .title(itemObj.optString("title", ""))
                .description(itemObj.optString("description", ""))
                .contentSummary(itemObj.optString("content_summary", ""))
                .itemType(itemObj.optString("item_type", ""))
                .canBePreviewed(itemObj.optBoolean("can_be_previewed", false))
                .isCodingExercise(itemObj.optBoolean("is_coding_exercise", false))
                .isPracticeTest(itemObj.optBoolean("is_practice_test", false))
                .previewUrl(itemObj.optString("preview_url", ""))
                .learnUrl(itemObj.optString("learn_url", ""))
                .objectIndex(itemObj.optInt("object_index", 0))
                .build();
    }
    
    private ReviewsSummaryDTO parseReviewsSummary(JSONObject reviewsResponse) {
        try {
            JSONArray results = reviewsResponse.optJSONArray("results");
            List<ReviewDTO> recentReviews = new ArrayList<>();
            
            if (results != null) {
                int maxReviews = Math.min(5, results.length());
                for (int i = 0; i < maxReviews; i++) {
                    JSONObject reviewObj = results.optJSONObject(i);
                    if (reviewObj != null) {
                        recentReviews.add(parseReview(reviewObj));
                    }
                }
            }
            
            return ReviewsSummaryDTO.builder()
                    .totalCount(reviewsResponse.optInt("count", 0))
                    .averageRating(null) // Calculate from reviews if needed
                    .recentReviews(recentReviews)
                    .build();
        } catch (Exception e) {
            log.error("Error parsing reviews summary", e);
            return null;
        }
    }
    
    private CourseReviewsDTO parseCourseReviews(JSONObject reviewsResponse, int page) {
        try {
            JSONArray results = reviewsResponse.optJSONArray("results");
            List<ReviewDTO> reviews = new ArrayList<>();
            
            if (results != null) {
                for (int i = 0; i < results.length(); i++) {
                    JSONObject reviewObj = results.optJSONObject(i);
                    if (reviewObj != null) {
                        reviews.add(parseReview(reviewObj));
                    }
                }
            }
            
            return CourseReviewsDTO.builder()
                    .reviews(reviews)
                    .totalCount(reviewsResponse.optInt("count", 0))
                    .currentPage(page)
                    .hasNext(reviewsResponse.optString("next", null) != null)
                    .hasPrevious(reviewsResponse.optString("previous", null) != null)
                    .nextUrl(reviewsResponse.optString("next", null))
                    .previousUrl(reviewsResponse.optString("previous", null))
                    .build();
        } catch (Exception e) {
            log.error("Error parsing course reviews", e);
            return null;
        }
    }
    
    private ReviewDTO parseReview(JSONObject reviewObj) {
        JSONObject userObj = reviewObj.optJSONObject("user");
        ReviewUserDTO user = null;
        if (userObj != null) {
            user = ReviewUserDTO.builder()
                    .displayName(userObj.optString("display_name", ""))
                    .publicDisplayName(userObj.optString("public_display_name", ""))
                    .image50x50(userObj.optString("image_50x50", ""))
                    .initials(userObj.optString("initials", ""))
                    .build();
        }
        
        JSONObject responseObj = reviewObj.optJSONObject("response");
        ReviewResponseDTO response = null;
        if (responseObj != null) {
            JSONObject responseUserObj = responseObj.optJSONObject("user");
            ReviewUserDTO responseUser = null;
            if (responseUserObj != null) {
                responseUser = ReviewUserDTO.builder()
                        .displayName(responseUserObj.optString("display_name", ""))
                        .publicDisplayName(responseUserObj.optString("public_display_name", ""))
                        .image50x50(responseUserObj.optString("image_50x50", ""))
                        .initials(responseUserObj.optString("initials", ""))
                        .build();
            }
            
            response = ReviewResponseDTO.builder()
                    .content(responseObj.optString("content", ""))
                    .contentHtml(responseObj.optString("content_html", ""))
                    .created(responseObj.optString("created", ""))
                    .createdFormatted(responseObj.optString("created_formatted_with_time_since", ""))
                    .user(responseUser)
                    .build();
        }
        
        return ReviewDTO.builder()
                .id(reviewObj.optLong("id", 0))
                .content(reviewObj.optString("content", ""))
                .contentHtml(reviewObj.optString("content_html", ""))
                .rating((float) reviewObj.optDouble("rating", 0.0))
                .created(reviewObj.optString("created", ""))
                .createdFormatted(reviewObj.optString("created_formatted_with_time_since", ""))
                .user(user)
                .response(response)
                .build();
    }
    
    private List<RelatedCourseDTO> parseRelatedCourses(JSONObject relatedCoursesResponse) {
        List<RelatedCourseDTO> relatedCourses = new ArrayList<>();
        
        try {
            JSONArray units = relatedCoursesResponse.optJSONArray("units");
            if (units != null && units.length() > 0) {
                JSONObject firstUnit = units.optJSONObject(0);
                if (firstUnit != null) {
                    JSONArray items = firstUnit.optJSONArray("items");
                    if (items != null) {
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject courseObj = items.optJSONObject(i);
                            if (courseObj != null) {
                                RelatedCourseDTO relatedCourse = parseRelatedCourse(courseObj);
                                if (relatedCourse != null) {
                                    relatedCourses.add(relatedCourse);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error parsing related courses", e);
        }
        
        return relatedCourses;
    }
    
    private RelatedCourseDTO parseRelatedCourse(JSONObject courseObj) {
        try {
            JSONArray instructors = courseObj.optJSONArray("visible_instructors");
            String author = "Unknown";
            if (instructors != null && instructors.length() > 0) {
                JSONObject instructor = instructors.optJSONObject(0);
                if (instructor != null) {
                    author = instructor.optString("display_name", "Unknown");
                }
            }
            
            return RelatedCourseDTO.builder()
                    .courseId(courseObj.optInt("id", 0))
                    .title(courseObj.optString("title", ""))
                    .headline(courseObj.optString("headline", ""))
                    .url("https://www.udemy.com" + courseObj.optString("url", ""))
                    .image240x135(courseObj.optString("image_240x135", ""))
                    .image480x270(courseObj.optString("image_480x270", ""))
                    .image750x422(courseObj.optString("image_750x422", ""))
                    .author(author)
                    .rating((float) courseObj.optDouble("rating", 0.0))
                    .numReviews(courseObj.optInt("num_reviews", 0))
                    .numSubscribers(courseObj.optInt("num_subscribers", 0))
                    .contentInfo(courseObj.optString("content_info_short", ""))
                    .instructionalLevel(courseObj.optString("instructional_level_simple", ""))
                    .build();
        } catch (Exception e) {
            log.error("Error parsing related course", e);
            return null;
        }
    }
    
    private PricingInfoDTO parsePricingInfo(JSONObject landingComponents) {
        try {
            JSONObject priceText = landingComponents.optJSONObject("price_text");
            if (priceText == null) {
                return null;
            }
            
            JSONObject data = priceText.optJSONObject("data");
            if (data == null) {
                return null;
            }
            
            JSONObject pricingResult = data.optJSONObject("pricing_result");
            if (pricingResult == null) {
                return null;
            }
            
            JSONObject price = pricingResult.optJSONObject("price");
            JSONObject listPrice = pricingResult.optJSONObject("list_price");
            JSONObject savingPrice = pricingResult.optJSONObject("saving_price");
            JSONObject campaign = pricingResult.optJSONObject("campaign");
            
            JSONObject discountExpiration = landingComponents.optJSONObject("discount_expiration");
            String discountDeadlineText = null;
            if (discountExpiration != null) {
                JSONObject discountData = discountExpiration.optJSONObject("data");
                if (discountData != null) {
                    discountDeadlineText = discountData.optString("discount_deadline_text", null);
                }
            }
            
            return PricingInfoDTO.builder()
                    .price(price != null ? (float) price.optDouble("amount", 0.0) : null)
                    .listPrice(listPrice != null ? (float) listPrice.optDouble("amount", 0.0) : null)
                    .savingPrice(savingPrice != null ? (float) savingPrice.optDouble("amount", 0.0) : null)
                    .currency(price != null ? price.optString("currency", "") : "")
                    .priceString(price != null ? price.optString("price_string", "") : "")
                    .currencySymbol(price != null ? price.optString("currency_symbol", "") : "")
                    .discountPercent(pricingResult.optInt("discount_percent_for_display", 0))
                    .discountDeadlineText(discountDeadlineText)
                    .couponCode(pricingResult.optString("code", ""))
                    .usesRemaining(campaign != null ? campaign.optInt("uses_remaining", 0) : null)
                    .maximumUses(campaign != null ? campaign.optInt("maximum_uses", 0) : null)
                    .build();
        } catch (Exception e) {
            log.error("Error parsing pricing info", e);
            return null;
        }
    }
    
    private IncentivesDTO parseIncentives(JSONObject landingComponents) {
        try {
            JSONObject incentives = landingComponents.optJSONObject("incentives");
            if (incentives == null) {
                return null;
            }
            
            return IncentivesDTO.builder()
                    .videoContentLength(incentives.optString("video_content_length", ""))
                    .numArticles(incentives.optInt("num_articles", 0))
                    .numQuizzes(incentives.optInt("num_quizzes", 0))
                    .numPracticeTests(incentives.optInt("num_practice_tests", 0))
                    .numCodingExercises(incentives.optInt("num_coding_exercises", 0))
                    .hasLifetimeAccess(incentives.optBoolean("has_lifetime_access", false))
                    .devicesAccess(incentives.optString("devices_access", ""))
                    .hasAssignments(incentives.optBoolean("has_assignments", false))
                    .hasCertificate(incentives.optBoolean("has_certificate", false))
                    .hasClosedCaptions(incentives.optBoolean("has_closed_captions", false))
                    .build();
        } catch (Exception e) {
            log.error("Error parsing incentives", e);
            return null;
        }
    }
    
    private List<PreviewVideoDTO> parsePreviewVideos(JSONObject previewPageJson) {
        List<PreviewVideoDTO> previewVideos = new ArrayList<>();
        
        try {
            JSONArray previews = previewPageJson.optJSONArray("previews");
            if (previews != null) {
                for (int i = 0; i < previews.length(); i++) {
                    JSONObject previewObj = previews.optJSONObject(i);
                    if (previewObj != null) {
                        PreviewVideoDTO previewVideo = parsePreviewVideo(previewObj);
                        if (previewVideo != null) {
                            previewVideos.add(previewVideo);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error parsing preview videos", e);
        }
        
        return previewVideos;
    }
    
    private PreviewVideoDTO parsePreviewVideo(JSONObject previewObj) {
        try {
            // Extract video URL from media_sources (HLS m3u8)
            String videoUrl = null;
            JSONArray mediaSources = previewObj.optJSONArray("media_sources");
            if (mediaSources != null && mediaSources.length() > 0) {
                JSONObject firstSource = mediaSources.optJSONObject(0);
                if (firstSource != null) {
                    videoUrl = firstSource.optString("src", "");
                }
            }
            
            // Extract stream URLs (MP4 files at different resolutions)
            List<VideoSourceDTO> streamUrls = new ArrayList<>();
            JSONObject streamUrlsObj = previewObj.optJSONObject("stream_urls");
            if (streamUrlsObj != null) {
                JSONArray videoStreams = streamUrlsObj.optJSONArray("Video");
                if (videoStreams != null) {
                    for (int i = 0; i < videoStreams.length(); i++) {
                        JSONObject streamObj = videoStreams.optJSONObject(i);
                        if (streamObj != null) {
                            VideoSourceDTO source = VideoSourceDTO.builder()
                                    .type(streamObj.optString("type", ""))
                                    .label(streamObj.optString("label", ""))
                                    .file(streamObj.optString("file", ""))
                                    .build();
                            if (!source.getFile().isEmpty()) {
                                streamUrls.add(source);
                            }
                        }
                    }
                }
            }
            
            return PreviewVideoDTO.builder()
                    .id(previewObj.optLong("id", 0))
                    .title(previewObj.optString("title", ""))
                    .thumbnailUrl(previewObj.optString("thumbnail_url", ""))
                    .contentSummary(previewObj.optString("content_summary", ""))
                    .timeEstimation(previewObj.optInt("time_estimation", 0))
                    .videoUrl(videoUrl)
                    .streamUrls(streamUrls.isEmpty() ? null : streamUrls)
                    .build();
        } catch (Exception e) {
            log.error("Error parsing preview video", e);
            return null;
        }
    }
}

