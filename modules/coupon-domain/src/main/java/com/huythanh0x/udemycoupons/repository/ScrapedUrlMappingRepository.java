package com.huythanh0x.udemycoupons.repository;

import com.huythanh0x.udemycoupons.model.coupon.ScrapedUrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * Repository for ScrapedUrlMapping entity.
 * Provides methods to query scraped URL to coupon URL mappings.
 */
@Repository
public interface ScrapedUrlMappingRepository extends JpaRepository<ScrapedUrlMapping, String> {
    
    /**
     * Finds a mapping by scraped URL.
     *
     * @param scrapedUrl The scraped URL to look up
     * @return The mapping if found, null otherwise
     */
    ScrapedUrlMapping findByScrapedUrl(String scrapedUrl);
    
    /**
     * Finds all mappings for a given coupon URL.
     * Useful for finding all scraped URLs that map to the same coupon.
     *
     * @param couponUrl The coupon URL to look up
     * @return List of mappings for the given coupon URL
     */
    List<ScrapedUrlMapping> findByCouponUrl(String couponUrl);
    
    /**
     * Finds all mappings for a given crawler source.
     *
     * @param crawlerSource The crawler source (e.g., "enext", "realdiscount")
     * @return List of mappings for the given crawler source
     */
    List<ScrapedUrlMapping> findByCrawlerSource(String crawlerSource);
    
    /**
     * Efficiently retrieves all coupon URLs from mappings without loading full entities.
     * Used for pre-validation to avoid fetching URLs already mapped.
     *
     * @return Set of all coupon URLs in mappings
     */
    @Query("SELECT s.couponUrl FROM ScrapedUrlMapping s WHERE s.couponUrl IS NOT NULL")
    Set<String> findAllCouponUrls();
}

