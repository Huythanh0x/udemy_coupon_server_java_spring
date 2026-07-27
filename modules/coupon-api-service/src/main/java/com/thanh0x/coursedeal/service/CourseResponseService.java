package com.thanh0x.coursedeal.service;

import com.thanh0x.coursedeal.crawler_runner.UdemyCouponCourseExtractor;
import com.thanh0x.coursedeal.dto.CouponDetailDTO;
import com.thanh0x.coursedeal.dto.CouponSummaryDTO;
import com.thanh0x.coursedeal.dto.PagedCouponResponseDTO;
import com.thanh0x.coursedeal.exception.BadRequestException;
import com.thanh0x.coursedeal.mapper.CouponMapper;
import com.thanh0x.coursedeal.model.coupon.CouponCourseData;
import com.thanh0x.coursedeal.model.coupon.CouponCourseHistory;
import com.thanh0x.coursedeal.repository.CouponCourseHistoryRepository;
import com.thanh0x.coursedeal.repository.CouponCourseRepository;
import com.thanh0x.coursedeal.repository.ExpiredCouponRepository;
import com.thanh0x.coursedeal.utils.LastFetchTimeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.thanh0x.coursedeal.utils.Constant;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for handling course response operations.
 */
@Service
public class CourseResponseService {
    private static final Logger log = LoggerFactory.getLogger(CourseResponseService.class);
    private final CouponCourseRepository couponCourseRepository;
    private final ExpiredCouponRepository expiredCouponRepository;
    private final CouponCourseHistoryRepository couponCourseHistoryRepository;
    private final CouponMapper couponMapper;
    private final UdemyScraperService udemyScraperService;


    @Autowired
    public CourseResponseService(CouponCourseRepository couponCourseRepository,
                                 ExpiredCouponRepository expiredCouponRepository,
                                 CouponCourseHistoryRepository couponCourseHistoryRepository,
                                 CouponMapper couponMapper,
                                 UdemyScraperService udemyScraperService) {
        this.couponCourseRepository = couponCourseRepository;
        this.expiredCouponRepository = expiredCouponRepository;
        this.couponCourseHistoryRepository = couponCourseHistoryRepository;
        this.couponMapper = couponMapper;
        this.udemyScraperService = udemyScraperService;
    }

    /**
     * Unified listing endpoint that supports basic pagination, structured filters, free-text search, and sorting.
     */
    public PagedCouponResponseDTO listCoupons(
        String category,
        String rating,
        String contentLength,
        String level,
        String language,
        String query,
        String sortBy,
        String sortOrder,
        String pageIndex,
        String numberPerPage,
        String remoteAddr
    ) {
        handlePagingParameters(pageIndex, numberPerPage);
        
        Sort sort = createSort(sortBy, sortOrder);
        Pageable pageable = PageRequest.of(
            Integer.parseInt(pageIndex), 
            Math.min(Integer.parseInt(numberPerPage), Constant.MAX_PAGE_SIZE),
            sort
        );

        boolean hasQuery = query != null && !query.isBlank();
        boolean hasStructuredFilters =
            (rating != null && !rating.equals("-1")) ||
                (contentLength != null && !contentLength.equals("-1")) ||
                (level != null && !level.isBlank()) ||
                (category != null && !category.isBlank()) ||
                (language != null && !language.isBlank());

        Page<CouponCourseData> page;

        if (hasQuery || hasStructuredFilters) {
             if (hasQuery) {
                page = couponCourseRepository.findByTitleContainingOrDescriptionContainingOrHeadingContaining(
                    query, query, query, pageable
                );
            } else {
                page = couponCourseRepository
                    .findByRatingGreaterThanAndContentLengthGreaterThanAndLevelContainingAndCategoryIsContainingIgnoreCaseAndLanguageContaining(
                        Float.parseFloat(rating),
                        Integer.parseInt(contentLength),
                        level,
                        category,
                        language,
                        pageable
                    );
            }
        } else {
            page = couponCourseRepository.findAll(pageable);
        }

        List<CouponSummaryDTO> dtos = page.getContent().stream()
                .map(couponMapper::toSummaryDto)
                .collect(Collectors.toList());

        return new PagedCouponResponseDTO(
                LastFetchTimeManager.loadLasFetchedTimeInMilliSecond(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getPageable().getPageNumber(),
                dtos
        );
    }

    /**
     * Creates a Sort object based on the provided sort field and order.
     */
    private Sort createSort(String sortBy, String sortOrder) {
        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "createdAt"; // Default sort
        }
        
        Sort.Direction direction = Sort.Direction.DESC; 
        if (sortOrder != null && sortOrder.equalsIgnoreCase("asc")) {
            direction = Sort.Direction.ASC;
        }
        
        String sortField;
        switch (sortBy.toLowerCase()) {
            case "students":
                sortField = "students";
                break;
            case "rating":
                sortField = "rating";
                break;
            case "createdat":
            case "created_at":
            case "newest":
                sortField = "createdAt";
                break;
            case "contentlength":
            case "content_length":
                sortField = "contentLength";
                break;
            case "usesremaining":
            case "uses_remaining":
                sortField = "usesRemaining";
                break;
            default:
                log.warn("Unknown sort field: {}, defaulting to createdAt", sortBy);
                sortField = "createdAt";
        }
        
        return Sort.by(direction, sortField);
    }

    /**
     * Saves a new coupon URL asynchronously.
     * 
     * @param couponUrl  the URL of the coupon to save
     * @param remoteAddr the remote address of the user saving the coupon
     */
    public void saveNewCouponUrlAsync(String couponUrl, String remoteAddr) {
        udemyScraperService.validateAndSaveCouponAsync(couponUrl, remoteAddr);
    }

    /**
     * Refreshes a coupon URL asynchronously.
     */
    public void refreshCouponAsync(Integer courseId, String remoteAddr) {
        couponCourseRepository.findById(courseId).ifPresent(coupon -> 
                udemyScraperService.validateAndSaveCouponAsync(coupon.getCouponUrl(), remoteAddr)
        );
    }

    /**
     * Saves a new coupon URL to the database.
     */
    public CouponDetailDTO saveNewCouponUrl(String couponUrl, String remoteAddr) {
        UdemyCouponCourseExtractor extractor = new UdemyCouponCourseExtractor(couponUrl);
        CouponCourseData couponData = extractor.getFullCouponCodeData();
        if (couponData == null) {
            throw new BadRequestException("Coupon is invalid or expired");
        }

        boolean existedBefore = couponCourseRepository.findByCouponUrl(couponUrl) != null
                || expiredCouponRepository.findByCouponUrl(couponUrl) != null;
        couponData.setNew(!existedBefore);

        CouponCourseData saved = couponCourseRepository.save(couponData);
        couponCourseHistoryRepository.save(CouponCourseHistory.builder()
            .courseId(saved.getCourseId())
            .title(saved.getTitle())
            .couponUrl(saved.getCouponUrl())
            .status(existedBefore ? "reactivated" : "new")
            .build());
        return couponMapper.toDetailDto(saved);
    }

    /**
     * Validates and handles paging parameters for pagination.
     */
    public void handlePagingParameters(String pageIndex, String numberPerPage) {
        try {
            Integer.parseInt(pageIndex);
            Integer.parseInt(numberPerPage);
        } catch (Exception e) {
            log.warn("Invalid paging parameters pageIndex={}, numberPerPage={}", pageIndex, numberPerPage, e);
            throw new BadRequestException(e.toString());
        }
        if (Integer.parseInt(pageIndex) < 0 || Integer.parseInt(numberPerPage) < 0) {
            throw new BadRequestException("Page index and number of course per page cannot be negative");
        }
    }

    /**
     * Retrieves the coupon course data by course ID.
     */
    public CouponDetailDTO getCouponDetail(String courseId) {
        CouponCourseData couponCourseData = couponCourseRepository.findByCourseId(Integer.parseInt(courseId));
        if (couponCourseData != null) {
            return couponMapper.toDetailDto(couponCourseData);
        } else {
            throw new BadRequestException("Course id not found");
        }
    }
}
